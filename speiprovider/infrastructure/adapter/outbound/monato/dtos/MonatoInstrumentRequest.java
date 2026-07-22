package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonatoInstrumentRequest(
        @JsonProperty("source_bank_id") String sourceBankId,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("type") String type,
        @JsonProperty("rfc") String rfc,
        @JsonProperty("alias") String alias,
        @JsonProperty("virtual_clabe") VirtualClabeDto virtualClabe
) {
    public record VirtualClabeDto(
            @JsonProperty("destination_bank_id") String destinationBankId,
            @JsonProperty("account_number") String accountNumber,
            @JsonProperty("clabe_number") String clabeNumber,
            @JsonProperty("holder_name") String holderName
    ) {}
}