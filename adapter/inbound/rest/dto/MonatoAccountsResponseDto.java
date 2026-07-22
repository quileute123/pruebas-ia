package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto;

import java.util.List;

import com.netpay.speiprovider.domain.model.AccountsResult;
import com.netpay.speiprovider.domain.model.ProviderAccount;

public record MonatoAccountsResponseDto(
        List<ProviderAccount> accounts,
        String sourceInstrumentId,
        String centralizingAccountType) {

    public static MonatoAccountsResponseDto from(AccountsResult result) {
        var centralizing = result.centralizingAccount();
        return new MonatoAccountsResponseDto(
                result.accounts(),
                centralizing.map(ProviderAccount::instrumentId).orElse(null),
                centralizing.map(ProviderAccount::accountType).orElse(null));
    }

}