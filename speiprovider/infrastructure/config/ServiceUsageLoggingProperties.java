package com.netpay.speiprovider.infrastructure.config;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Configuracion del logging de uso de servicios inbound (START/END/ERROR + payloads).
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spei.logging.service-usage")
public class ServiceUsageLoggingProperties {

	/** Si false, el interceptor/filter no se registran. */
	private boolean enabled = true;

	/** Si true, imprime request/response en formato JSON. */
	private boolean logPayload = true;

	/** Tamano maximo (bytes) a cachear/loguear del body. */
	private int maxPayloadBytes = 16_384;

	/** Header de correlacion; si no viene, se genera un UUID. */
	private String requestIdHeader = "X-Request-Id";

	/** Header del proveedor SPEI. */
	private String providerHeader = "X-SPEI-Provider";

	/**
	 * Paths (servlet path, sin context-path) a instrumentar.
	 * Soporta patrones Ant ({@code /**}, {@code *}, etc.).
	 */
	private List<String> paths = new ArrayList<>(List.of(
			"/api/v1/spei/accounts",
			"/api/v1/spei/payout",
			"/api/v1/spei/destinations"));

	/** Campos sensibles a enmascarar dentro del JSON. */
	private List<String> sensitiveFields = new ArrayList<>(List.of(
			"password",
			"clientSecret",
			"client_secret",
			"apiKey",
			"api_key",
			"token",
			"authorization",
			"clabeNumber",
			"clabe"));
}
