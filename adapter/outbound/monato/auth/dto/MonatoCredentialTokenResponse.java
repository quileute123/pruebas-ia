package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonatoCredentialTokenResponse {

	private String token;

	@JsonProperty("access_token")
	private String accessToken;

	public String resolveToken() {
		return switch (token) {
			case String value when !value.isBlank() -> value;
			case null, default -> accessToken;
		};
	}

}
