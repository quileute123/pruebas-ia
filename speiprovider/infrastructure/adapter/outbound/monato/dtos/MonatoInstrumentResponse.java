package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MonatoInstrumentResponse(
        @JsonProperty("id") String id,
        @JsonProperty("bankId") String bankId,
        @JsonProperty("clientId") String clientId,
        @JsonProperty("ownerId") String ownerId,
        @JsonProperty("alias") String alias,
        @JsonProperty("type") String type,
        @JsonProperty("instrumentDetail") InstrumentDetailDto instrumentDetail,
        @JsonProperty("audit") AuditDto audit,
        @JsonProperty("rfc") String rfc
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InstrumentDetailDto(
            @JsonProperty("accountNumber") String accountNumber,
            @JsonProperty("clabeNumber") String clabeNumber,
            @JsonProperty("holderName") String holderName
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AuditDto(
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt,
            @JsonProperty("deletedAt") String deletedAt,
            @JsonProperty("blockedAt") String blockedAt
    ) {}
}