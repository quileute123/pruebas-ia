package com.netpay.speiprovider.domain.model;

public record PaymentDestination(
        String sourceBankId,
        String clientId,
        String type,
        String rfc,
        String alias,
        String destinationBankId,
        String accountNumber,
        String clabeNumber,
        String holderName
) {}