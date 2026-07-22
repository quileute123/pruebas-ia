package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonatoErrorResponse {

	private int code;
	private String message;
	private List<ErrorDetail> details;

	public String resolveDetailMessage() {
		ErrorMetadata metadata = resolveMetadata();
		if (metadata == null) {
			return message;
		}
		String detail = metadata.getErrorDetail();
		return detail != null && !detail.isBlank() ? detail : message;
	}

	public ErrorMetadata resolveMetadata() {
		ErrorDetail firstDetail = resolveFirstDetail();
		return firstDetail != null ? firstDetail.getMetadata() : null;
	}

	private ErrorDetail resolveFirstDetail() {
		if (details == null || details.isEmpty()) {
			return null;
		}
		return details.getFirst();
	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorDetail {

		private String reason;
		private String domain;
		private ErrorMetadata metadata;

	}

	@Getter
	@Setter
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class ErrorMetadata {

		@JsonProperty("error_detail")
		private String errorDetail;

		@JsonProperty("http_code")
		private String httpCode;

		private String module;

		@JsonProperty("method_name")
		private String methodName;

		@JsonProperty("error_code")
		private String errorCode;

	}

}
