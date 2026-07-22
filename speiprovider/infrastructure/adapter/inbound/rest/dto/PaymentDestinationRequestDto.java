package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto;

import com.netpay.speiprovider.domain.model.PaymentDestination;

public record PaymentDestinationRequestDto(
        String sourceBankId,
        String clientId,
        String type,
        String rfc,
        String alias,
        String destinationBankId,
        String accountNumber,
        String clabeNumber,
        String holderName
) {
    public PaymentDestination toDomain() {
        return new PaymentDestination(
                this.sourceBankId(),
                this.clientId(),
                this.type(),
                this.rfc(),
                this.alias(),
                this.destinationBankId(),
                this.accountNumber(),
                this.clabeNumber(),
                this.holderName()
        );
    }
}