package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonatoCredentialsResponse {

	private List<Credential> data;

	@Getter
	@Builder
	@Jacksonized
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Credential {

		private String id;

		@JsonProperty("client_secret")
		private String clientSecret;

	}

	public String resolveClientSecret() {
		if (data == null || data.isEmpty()) {
			return null;
		}
		return data.getFirst().getClientSecret();
	}

}
