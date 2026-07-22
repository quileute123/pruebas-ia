package com.netpay.speiprovider.domain.model;

import java.math.BigDecimal;

public record PayoutTransfer(
        String clientId,
        String paymentDestinationId,
        BigDecimal amount,
        String reference,
        String concept,
        String currency
) {}