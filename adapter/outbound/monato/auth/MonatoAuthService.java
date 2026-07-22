package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto.MonatoCredentialTokenRequest;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto.MonatoCredentialTokenResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto.MonatoCredentialsResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;

import lombok.extern.slf4j.Slf4j;

/**
 * Gestiona la autenticacion con Monato: obtiene, cachea y renueva el JWT de credential-tokens.
 * El token se reutiliza en memoria hasta que esta proximo a expirar; la renovacion es thread-safe.
 */
@Slf4j
@Service
public class MonatoAuthService {

	private static final String GET_CREDENTIALS = "GET /v1/clients/{clientId}/credentials";
	private static final String CREATE_TOKEN = "POST /v1/clients/{clientId}/auth/credential-tokens";
	private static final long DEFAULT_TOKEN_TTL_SECONDS = 3600;

	private final MonatoProperties monatoProperties;
	private final ObjectMapper objectMapper;
	private final RestClient monatoAuthRestClient;
	private final MonatoErrorParser monatoErrorParser;

	/** Evita renovaciones concurrentes cuando varios hilos detectan el token vencido al mismo tiempo. */
	private final ReentrantLock tokenLock = new ReentrantLock();

	private volatile String cachedToken;
	private volatile Instant tokenExpiresAt = Instant.EPOCH;

	public MonatoAuthService(MonatoProperties monatoProperties, ObjectMapper objectMapper,
			@Qualifier("monatoAuthRestClient") RestClient monatoAuthRestClient, MonatoErrorParser monatoErrorParser) {
		this.monatoProperties = monatoProperties;
		this.objectMapper = objectMapper;
		this.monatoAuthRestClient = monatoAuthRestClient;
		this.monatoErrorParser = monatoErrorParser;
	}

	/**
	 * Devuelve un token vigente. Si el cache expiro o no existe, solicita uno nuevo a Monato.
	 */
	public String getValidToken() {
		if (isTokenValid()) {
			return cachedToken;
		}
		refreshToken();
		if (!isTokenValid()) {
			throw new MonatoIntegrationException(CREATE_TOKEN, HttpStatus.BAD_GATEWAY,
					"No fue posible obtener un token valido de Monato", null, null);
		}
		return cachedToken;
	}

	/**
	 * Renueva el token bajo lock. Revalida el cache dentro del lock por si otro hilo ya renovo.
	 */
	public void refreshToken() {
		tokenLock.lock();
		try {
			if (isTokenValid()) {
				return;
			}

			var clientSecret = resolveClientSecret();
			var token = requestCredentialToken(clientSecret);
			cachedToken = token;
			tokenExpiresAt = resolveTokenExpiry(token);
			log.info("Token Monato renovado. Expira en {}", tokenExpiresAt);
		}
		finally {
			tokenLock.unlock();
		}
	}

	/**
	 * Considera valido el token si aun no alcanza la expiracion menos el skew configurado
	 * ({@code monato.token-refresh-skew-seconds}), para renovar antes de que Monato lo rechace.
	 */
	private boolean isTokenValid() {
		return cachedToken != null
				&& tokenExpiresAt.isAfter(Instant.now().plusSeconds(monatoProperties.getTokenRefreshSkewSeconds()));
	}

	/**
	 * Usa el client-secret de configuracion; si no existe, lo obtiene de GET /credentials
	 * cuando {@code fetch-credentials-on-refresh} esta habilitado.
	 */
	private String resolveClientSecret() {
		return Optional.ofNullable(monatoProperties.getClientSecret())
				.filter(secret -> !secret.isBlank())
				.orElseGet(() -> {
					if (!monatoProperties.isFetchCredentialsOnRefresh()) {
						throw new IllegalArgumentException(
								"client-secret no configurado y fetch-credentials-on-refresh=false");
					}
					return fetchClientSecret();
				});
	}

	/** Consulta Monato para obtener el client_secret cuando no esta en propiedades locales. */
	private String fetchClientSecret() {
		try {
			var response = monatoAuthRestClient.get()
					.uri("/v1/clients/{clientId}/credentials", monatoProperties.getClientId())
					.retrieve()
					.body(MonatoCredentialsResponse.class);

			var clientSecret = response != null ? response.resolveClientSecret() : null;
			if (clientSecret == null || clientSecret.isBlank()) {
				throw new MonatoIntegrationException(GET_CREDENTIALS, HttpStatus.BAD_GATEWAY,
						"Monato no devolvio client_secret en GET /credentials", null, null);
			}
			return clientSecret;
		}
		catch (RestClientResponseException ex) {
			throw monatoErrorParser.toException(GET_CREDENTIALS, ex);
		}
	}

	/**
	 * Intercambia client_id + client_secret por un JWT en POST /auth/credential-tokens.
	 * Usa monatoAuthRestClient (con x-api-key, sin Bearer) porque este endpoint es previo al token.
	 */
	private String requestCredentialToken(String clientSecret) {
		try {
			var response = monatoAuthRestClient.post()
					.uri("/v1/clients/{clientId}/auth/credential-tokens", monatoProperties.getClientId())
					.contentType(MediaType.APPLICATION_JSON)
					.body(MonatoCredentialTokenRequest.of(monatoProperties.getClientId(), clientSecret))
					.retrieve()
					.body(MonatoCredentialTokenResponse.class);

			var token = response != null ? response.resolveToken() : null;
			if (token == null || token.isBlank()) {
				throw new MonatoIntegrationException(CREATE_TOKEN, HttpStatus.BAD_GATEWAY,
						"Monato no devolvio token en POST /auth/credential-tokens", null, null);
			}
			return token;
		}
		catch (RestClientResponseException ex) {
			throw monatoErrorParser.toException(CREATE_TOKEN, ex);
		}
	}

	/**
	 * Extrae la expiracion del claim {@code exp} del JWT. Si no es decodificable, asume 1 hora.
	 */
	private Instant resolveTokenExpiry(String token) {
		try {
			var parts = token.split("\\.");
			if (parts.length < 2) {
				return Instant.now().plusSeconds(DEFAULT_TOKEN_TTL_SECONDS);
			}
			var decoded = Base64.getUrlDecoder().decode(parts[1]);
			var payload = objectMapper.readTree(new String(decoded, StandardCharsets.UTF_8));
			if (payload.hasNonNull("exp")) {
				return Instant.ofEpochSecond(payload.get("exp").asLong());
			}
		}
		catch (Exception ex) {
			log.warn("No se pudo leer exp del JWT Monato, se usara TTL por defecto", ex);
		}
		return Instant.now().plusSeconds(DEFAULT_TOKEN_TTL_SECONDS);
	}

}
