package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonatoCredentialTokenRequest(
		@JsonProperty("client_id") String clientId,
		@JsonProperty("client_secret") String clientSecret) {

	public static MonatoCredentialTokenRequest of(String clientId, String clientSecret) {
		return new MonatoCredentialTokenRequest(clientId, clientSecret);
	}

}
