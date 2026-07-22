package com.netpay.speiprovider.infrastructure.config;

import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Interceptor de uso de servicios inbound: marca inicio y fin de cada request
 * instrumentado con formato uniforme y correlacion por requestId.
 * Si {@code log-payload=true}, tambien imprime request/response JSON.
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceUsageLoggingInterceptor implements HandlerInterceptor {

	static final String ATTR_START_NANOS = ServiceUsageLoggingInterceptor.class.getName() + ".startNanos";
	static final String ATTR_REQUEST_ID = ServiceUsageLoggingInterceptor.class.getName() + ".requestId";
	public static final String ATTR_CLIENT_ID = ServiceUsageLoggingInterceptor.class.getName() + ".clientId";
	static final String ATTR_TRACKED = ServiceUsageLoggingInterceptor.class.getName() + ".tracked";

	private final ServiceUsageLoggingProperties properties;
	private final MonatoProperties monatoProperties;
	@Nullable
	private final ServiceUsagePayloadCachingFilter payloadFilter;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler) {
		if (!shouldTrack(request)) {
			return true;
		}

		String requestId = resolveRequestId(request);
		String clientId = resolveClientId(request);
		request.setAttribute(ATTR_TRACKED, Boolean.TRUE);
		request.setAttribute(ATTR_START_NANOS, System.nanoTime());
		request.setAttribute(ATTR_REQUEST_ID, requestId);
		request.setAttribute(ATTR_CLIENT_ID, clientId);
		response.setHeader(properties.getRequestIdHeader(), requestId);

		log.info("[SERVICE_START] operation={} method={} path={} provider={} clientId={} requestId={}",
				resolveOperation(request),
				request.getMethod(),
				request.getServletPath(),
				resolveProvider(request),
				clientId,
				requestId);

		return true;
	}

	@Override
	public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
			@NonNull Object handler, Exception ex) {
		if (!Boolean.TRUE.equals(request.getAttribute(ATTR_TRACKED))) {
			return;
		}

		String requestId = String.valueOf(request.getAttribute(ATTR_REQUEST_ID));
		String clientId = String.valueOf(request.getAttribute(ATTR_CLIENT_ID));
		long durationMs = resolveDurationMs(request);
		String operation = resolveOperation(request);
		String method = request.getMethod();
		String path = request.getServletPath();
		String provider = resolveProvider(request);
		int status = response.getStatus();

		logPayloads(request, response, operation, requestId);

		if (ex != null) {
			log.error(
					"[SERVICE_ERROR] operation={} method={} path={} provider={} clientId={} status={} durationMs={} error={} message={} requestId={}",
					operation, method, path, provider, clientId, status, durationMs,
					ex.getClass().getSimpleName(), safeMessage(ex), requestId);
			return;
		}

		log.info(
				"[SERVICE_END] operation={} method={} path={} provider={} clientId={} status={} durationMs={} requestId={}",
				operation, method, path, provider, clientId, status, durationMs, requestId);
	}

	private void logPayloads(HttpServletRequest request, HttpServletResponse response, String operation,
			String requestId) {
		if (!properties.isLogPayload() || payloadFilter == null) {
			return;
		}
		try {
			String requestJson = payloadFilter.buildRequestJson(request);
			String responseJson = payloadFilter.buildResponseJson(request, response);
			log.info("[SERVICE_REQUEST] operation={} requestId={}\n{}", operation, requestId, requestJson);
			log.info("[SERVICE_RESPONSE] operation={} requestId={}\n{}", operation, requestId, responseJson);
		}
		catch (Exception payloadEx) {
			log.warn("[SERVICE_PAYLOAD_WARN] operation={} requestId={} message={}", operation, requestId,
					payloadEx.getMessage());
		}
	}

	public boolean shouldTrack(HttpServletRequest request) {
		String servletPath = request.getServletPath();
		if (servletPath == null || servletPath.isBlank()) {
			return false;
		}
		return properties.getPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, servletPath));
	}

	private String resolveRequestId(HttpServletRequest request) {
		String headerName = properties.getRequestIdHeader();
		String incoming = request.getHeader(headerName);
		if (incoming != null && !incoming.isBlank()) {
			return incoming.trim();
		}
		return UUID.randomUUID().toString();
	}

	private String resolveProvider(HttpServletRequest request) {
		String provider = request.getHeader(properties.getProviderHeader());
		if (provider == null || provider.isBlank()) {
			return "MONATO";
		}
		return provider.trim();
	}

	/**
	 * Misma precedencia que los adapters: query param {@code clientId}; si no viene,
	 * usa {@code monato.client-id} de configuracion.
	 */
	private String resolveClientId(HttpServletRequest request) {
		String clientId = request.getParameter("clientId");
		if (clientId != null && !clientId.isBlank()) {
			return clientId.trim();
		}
		if (monatoProperties != null) {
			String configured = monatoProperties.getClientId();
			if (configured != null && !configured.isBlank()) {
				return configured.trim();
			}
		}
		return "-";
	}

	private String resolveOperation(HttpServletRequest request) {
		String path = request.getServletPath();
		if (path == null) {
			return "UNKNOWN";
		}
		if (pathMatcher.match("/api/v1/spei/accounts", path)
				|| pathMatcher.match("/api/v1/spei/accounts/**", path)) {
			return "ACCOUNTS";
		}
		if (pathMatcher.match("/api/v1/spei/payout", path)
				|| pathMatcher.match("/api/v1/spei/payout/**", path)) {
			return "PAYOUT";
		}
		if (pathMatcher.match("/api/v1/spei/destinations", path)
				|| pathMatcher.match("/api/v1/spei/destinations/**", path)) {
			return "PAYMENT_DESTINATION";
		}
		if (pathMatcher.match("/api/v1/spei/banks/sync", path)
				|| pathMatcher.match("/api/v1/spei/banks/sync/**", path)) {
			return "BANKS_SYNC";
		}
		if (pathMatcher.match("/api/v1/spei/banks", path)
				|| pathMatcher.match("/api/v1/spei/banks/**", path)) {
			return "BANKS";
		}
		return "UNKNOWN";
	}

	private long resolveDurationMs(HttpServletRequest request) {
		Object start = request.getAttribute(ATTR_START_NANOS);
		if (!(start instanceof Long startNanos)) {
			return -1L;
		}
		return (System.nanoTime() - startNanos) / 1_000_000L;
	}

	private String safeMessage(Exception ex) {
		String message = ex.getMessage();
		if (message == null || message.isBlank()) {
			return "-";
		}
		return message.replace('\n', ' ').replace('\r', ' ').trim();
	}
}
