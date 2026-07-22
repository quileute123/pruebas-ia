package com.netpay.speiprovider.domain.model;

public record PayoutResponse(
        String transactionId,
        String trackingId,
        String reference,
        String status
) {}
