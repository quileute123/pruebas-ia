package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MonatoMoneyOutResponse(
        @JsonProperty("id") String id,
        @JsonProperty("bankId") String bankId,
        @JsonProperty("clientId") String clientId,
        @JsonProperty("externalReference") String externalReference,
        @JsonProperty("trackingId") String trackingId,
        @JsonProperty("description") String description,
        @JsonProperty("amount") String amount,
        @JsonProperty("currency") String currency,
        @JsonProperty("category") String category,
        @JsonProperty("subCategory") String subCategory,
        @JsonProperty("transactionStatus") String transactionStatus,
        @JsonProperty("audit") AuditDto audit
) {
    public record AuditDto(
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt,
            @JsonProperty("deletedAt") String deletedAt,
            @JsonProperty("blockedAt") String blockedAt
    ) {}
}