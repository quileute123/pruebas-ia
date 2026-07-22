package com.netpay.speiprovider.domain.model;

public record ProviderAccount(
        String id,
        String accountType,
        String instrumentId
) {}