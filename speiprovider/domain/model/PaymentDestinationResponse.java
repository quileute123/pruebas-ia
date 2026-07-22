package com.netpay.speiprovider.domain.model;

public record PaymentDestinationResponse(
        String destinationId,
        String status
) {}