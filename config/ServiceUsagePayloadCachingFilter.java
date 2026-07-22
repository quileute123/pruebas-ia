package com.netpay.speiprovider.infrastructure.config;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Cachea request/response body para poder loguearlos en JSON sin consumir el stream.
 * Solo aplica a paths instrumentados cuando {@code log-payload=true}.
 */
@Slf4j
@RequiredArgsConstructor
public class ServiceUsagePayloadCachingFilter extends OncePerRequestFilter {

	public static final String ATTR_CACHED_REQUEST = ServiceUsagePayloadCachingFilter.class.getName() + ".cachedRequest";
	public static final String ATTR_CACHED_RESPONSE = ServiceUsagePayloadCachingFilter.class.getName() + ".cachedResponse";

	private final ServiceUsageLoggingProperties properties;
	private final ObjectMapper objectMapper;
	private final AntPathMatcher pathMatcher = new AntPathMatcher();

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		if (!properties.isEnabled() || !properties.isLogPayload()) {
			return true;
		}
		return !matches(request.getServletPath());
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		ContentCachingRequestWrapper cachingRequest =
				new ContentCachingRequestWrapper(request, properties.getMaxPayloadBytes());
		ContentCachingResponseWrapper cachingResponse = new ContentCachingResponseWrapper(response);

		cachingRequest.setAttribute(ATTR_CACHED_REQUEST, cachingRequest);
		cachingRequest.setAttribute(ATTR_CACHED_RESPONSE, cachingResponse);

		try {
			filterChain.doFilter(cachingRequest, cachingResponse);
		}
		finally {
			cachingResponse.copyBodyToResponse();
		}
	}

	boolean matches(String servletPath) {
		if (!StringUtils.hasText(servletPath)) {
			return false;
		}
		return properties.getPaths().stream().anyMatch(pattern -> pathMatcher.match(pattern, servletPath));
	}

	/**
	 * Construye JSON pretty-printed del request:
	 * - Con body (POST/PUT/PATCH): el JSON del body tal cual
	 * - Sin body (GET/DELETE): query params como objeto JSON
	 * - Si no hay query clientId, incluye el clientId resuelto (config/fallback)
	 */
	public String buildRequestJson(HttpServletRequest request) {
		try {
			byte[] body = extractRequestBody(request);
			if (body.length > 0) {
				JsonNode bodyNode = parseBodyNode(body, request.getCharacterEncoding(), request.getContentType());
				return toPrettyJson(maskSensitive(ensureClientId(bodyNode, request)));
			}

			Map<String, String> query = extractQueryParams(request);
			enrichClientId(query, request);
			if (!query.isEmpty()) {
				return toPrettyJson(maskSensitive(objectMapper.valueToTree(query)));
			}
			return "{}";
		}
		catch (Exception ex) {
			log.debug("No se pudo serializar request JSON: {}", ex.getMessage());
			return "{\n  \"error\" : \"unserializable_request\"\n}";
		}
	}

	/**
	 * Si el mapa no trae clientId, usa el resuelto por el interceptor
	 * (query param o {@code monato.client-id}).
	 */
	private void enrichClientId(Map<String, String> payload, HttpServletRequest request) {
		String existing = payload.get("clientId");
		if (existing != null && !existing.isBlank()) {
			return;
		}
		Object resolved = request.getAttribute(ServiceUsageLoggingInterceptor.ATTR_CLIENT_ID);
		if (resolved == null) {
			return;
		}
		String clientId = String.valueOf(resolved).trim();
		if (!clientId.isEmpty() && !"-".equals(clientId)) {
			payload.put("clientId", clientId);
		}
	}

	private JsonNode ensureClientId(JsonNode node, HttpServletRequest request) {
		if (node == null || !node.isObject()) {
			return node;
		}
		ObjectNode object = (ObjectNode) node;
		JsonNode existing = object.get("clientId");
		if (existing != null && !existing.asText("").isBlank()) {
			return object;
		}
		Object resolved = request.getAttribute(ServiceUsageLoggingInterceptor.ATTR_CLIENT_ID);
		if (resolved == null) {
			return object;
		}
		String clientId = String.valueOf(resolved).trim();
		if (!clientId.isEmpty() && !"-".equals(clientId)) {
			object.put("clientId", clientId);
		}
		return object;
	}

	public String buildResponseJson(HttpServletRequest request, HttpServletResponse response) {
		try {
			byte[] body = extractResponseBody(request);
			if (body.length == 0) {
				return "{}";
			}
			JsonNode node = parseBodyNode(body, response.getCharacterEncoding(), response.getContentType());
			return toPrettyJson(maskSensitive(node));
		}
		catch (Exception ex) {
			log.debug("No se pudo serializar response JSON: {}", ex.getMessage());
			return "{\n  \"error\" : \"unserializable_response\"\n}";
		}
	}

	private String toPrettyJson(JsonNode node) throws Exception {
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
	}

	private Map<String, String> extractQueryParams(HttpServletRequest request) {
		Map<String, String> query = new LinkedHashMap<>();
		var parameterMap = request.getParameterMap();
		if (parameterMap == null || parameterMap.isEmpty()) {
			return query;
		}
		parameterMap.forEach((key, values) -> {
			if (values == null || values.length == 0) {
				query.put(key, "");
			}
			else if (values.length == 1) {
				query.put(key, values[0]);
			}
			else {
				query.put(key, String.join(",", values));
			}
		});
		return query;
	}

	private byte[] extractRequestBody(HttpServletRequest request) {
		ContentCachingRequestWrapper wrapper = resolveRequestWrapper(request);
		if (wrapper == null) {
			return new byte[0];
		}
		return truncate(wrapper.getContentAsByteArray());
	}

	private byte[] extractResponseBody(HttpServletRequest request) {
		Object attr = request.getAttribute(ATTR_CACHED_RESPONSE);
		if (attr instanceof ContentCachingResponseWrapper wrapper) {
			return truncate(wrapper.getContentAsByteArray());
		}
		return new byte[0];
	}

	private ContentCachingRequestWrapper resolveRequestWrapper(HttpServletRequest request) {
		Object attr = request.getAttribute(ATTR_CACHED_REQUEST);
		if (attr instanceof ContentCachingRequestWrapper wrapper) {
			return wrapper;
		}
		if (request instanceof ContentCachingRequestWrapper wrapper) {
			return wrapper;
		}
		return null;
	}

	private JsonNode parseBodyNode(byte[] body, String encoding, String contentType) throws Exception {
		Charset charset = resolveCharset(encoding);
		String raw = new String(body, charset).trim();
		if (raw.isEmpty()) {
			return objectMapper.createObjectNode();
		}
		if (isJsonContent(contentType, raw)) {
			return objectMapper.readTree(raw);
		}
		return objectMapper.getNodeFactory().textNode(raw);
	}

	private JsonNode maskSensitive(JsonNode node) {
		if (node == null || node.isNull()) {
			return node;
		}
		if (node.isObject()) {
			ObjectNode object = (ObjectNode) node;
			var fields = new java.util.ArrayList<String>();
			object.fieldNames().forEachRemaining(fields::add);
			for (String field : fields) {
				if (isSensitive(field)) {
					object.put(field, "***");
				}
				else {
					object.set(field, maskSensitive(object.get(field)));
				}
			}
			return object;
		}
		if (node.isArray()) {
			for (int i = 0; i < node.size(); i++) {
				((com.fasterxml.jackson.databind.node.ArrayNode) node).set(i, maskSensitive(node.get(i)));
			}
		}
		return node;
	}

	private boolean isSensitive(String field) {
		if (field == null) {
			return false;
		}
		String normalized = field.trim();
		return properties.getSensitiveFields().stream().anyMatch(s -> s.equalsIgnoreCase(normalized));
	}

	private boolean isJsonContent(String contentType, String raw) {
		if (StringUtils.hasText(contentType) && contentType.toLowerCase().contains(MediaType.APPLICATION_JSON_VALUE)) {
			return true;
		}
		return raw.startsWith("{") || raw.startsWith("[");
	}

	private Charset resolveCharset(String encoding) {
		if (!StringUtils.hasText(encoding)) {
			return StandardCharsets.UTF_8;
		}
		try {
			return Charset.forName(encoding);
		}
		catch (Exception ex) {
			return StandardCharsets.UTF_8;
		}
	}

	private byte[] truncate(byte[] content) {
		if (content == null || content.length == 0) {
			return new byte[0];
		}
		int max = Math.max(properties.getMaxPayloadBytes(), 0);
		if (max == 0 || content.length <= max) {
			return content;
		}
		byte[] truncated = new byte[max];
		System.arraycopy(content, 0, truncated, 0, max);
		return truncated;
	}
}
