package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth;

import java.io.IOException;
import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;

import lombok.RequiredArgsConstructor;

/**
 * Interceptor del RestClient de Monato: inyecta Authorization y x-api-key en cada peticion.
 * Si Monato responde 401, invalida el cache renovando el token y reintenta una vez.
 */
@Component
@RequiredArgsConstructor
public class MonatoAuthInterceptor implements ClientHttpRequestInterceptor {

	private final MonatoAuthService monatoAuthService;
	private final MonatoProperties monatoProperties;

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
			throws IOException {
		var response = execution.execute(withAuthHeaders(request, monatoAuthService.getValidToken()), body);

		// Token expirado o revocado en Monato: renovar y reintentar con credenciales frescas.
		if (response.getStatusCode().isSameCodeAs(HttpStatus.UNAUTHORIZED)) {
			response.close();
			monatoAuthService.refreshToken();
			return execution.execute(withAuthHeaders(request, monatoAuthService.getValidToken()), body);
		}

		return response;
	}

	/** Clona la peticion original agregando Bearer token y api-key sin mutar el request entrante. */
	private HttpRequest withAuthHeaders(HttpRequest request, String token) {
		return new HttpRequest() {
			@Override
			public HttpMethod getMethod() {
				return request.getMethod();
			}

			@Override
			public URI getURI() {
				return request.getURI();
			}

			@Override
			public HttpHeaders getHeaders() {
				var headers = new HttpHeaders();
				headers.putAll(request.getHeaders());
				headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
				headers.set("x-api-key", monatoProperties.getApiKey());
				return headers;
			}
		};
	}

}
