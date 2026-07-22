package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception;

import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public class MonatoIntegrationException extends RuntimeException {

	private final String operation;
	private final HttpStatusCode upstreamStatus;
	private final MonatoErrorResponse providerError;

	public MonatoIntegrationException(String operation, HttpStatusCode upstreamStatus, String message,
			MonatoErrorResponse providerError, Throwable cause) {
		super(message, cause);
		this.operation = operation;
		this.upstreamStatus = upstreamStatus;
		this.providerError = providerError;
	}

}
