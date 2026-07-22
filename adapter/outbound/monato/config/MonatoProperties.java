package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Propiedades de integracion con Monato, mapeadas desde el prefijo {@code monato.*} en application.yml.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "monato")
public class MonatoProperties {

	/** URL base de la API (ej. https://apicore.stg.finch.lat). */
	private String baseUrl;

	/** API key enviada en header x-api-key en todas las peticiones. */
	private String apiKey;

	/** Identificador del cliente en Monato; se usa en rutas /v1/clients/{clientId}/... */
	private String clientId;

	/** Secret para obtener el JWT; si falta, puede resolverse via GET /credentials. */
	private String clientSecret;

	/** Si true y no hay client-secret configurado, lo obtiene de Monato al renovar el token. */
	private boolean fetchCredentialsOnRefresh = true;

	/** Segundos antes de la expiracion en los que se considera el token como vencido y se renueva. */
	private long tokenRefreshSkewSeconds = 60;

    private String sourceInstrumentId;

}
