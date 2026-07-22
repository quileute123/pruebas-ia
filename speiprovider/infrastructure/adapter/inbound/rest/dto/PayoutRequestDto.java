package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto;

import java.math.BigDecimal;

public record PayoutRequestDto(
        String clientId,
        String destinationId,
        String reference,
        String concept,
        BigDecimal amount,
        String currency
) {}