package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception;

import java.util.Optional;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MonatoErrorParser {

	private final ObjectMapper objectMapper;

	public MonatoErrorResponse parse(String responseBody) {
		if (responseBody == null || responseBody.isBlank()) {
			return null;
		}
		try {
			return objectMapper.readValue(responseBody, MonatoErrorResponse.class);
		}
		catch (Exception ex) {
			return null;
		}
	}

	public MonatoIntegrationException toException(String operation, RestClientResponseException ex) {
		var errorResponse = parse(ex.getResponseBodyAsString());
		var statusCode = ex.getStatusCode();

		var message = Optional.ofNullable(errorResponse)
				.map(MonatoErrorResponse::resolveDetailMessage)
				.filter(detail -> !detail.isBlank())
				.or(() -> Optional.ofNullable(ex.getResponseBodyAsString()).filter(body -> !body.isBlank()))
				.orElseGet(() -> "Error en integracion Monato durante " + operation);

		return new MonatoIntegrationException(operation, statusCode, message, errorResponse, ex);
	}

}
