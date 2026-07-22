package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;

public record MonatoMoneyOutRequest(
        @JsonProperty("client_id") String clientId,
        @JsonProperty("source_instrument_id") String sourceInstrumentId,
        @JsonProperty("destination_instrument_id") String destinationInstrumentId,
        @JsonProperty("transaction_request") TransactionRequestDto transactionRequest
) {
    public record TransactionRequestDto(
            @JsonProperty("external_reference") String externalReference,
            @JsonProperty("description") String description,
            @JsonProperty("amount") String amount,
            @JsonProperty("currency") String currency
    ) {}
}