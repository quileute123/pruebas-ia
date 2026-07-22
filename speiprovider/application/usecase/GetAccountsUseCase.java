package com.netpay.speiprovider.application.usecase;

import org.springframework.stereotype.Service;

import com.netpay.speiprovider.domain.model.AccountsResult;
import com.netpay.speiprovider.domain.port.inbound.GetAccountsPort;
import com.netpay.speiprovider.domain.port.outbound.AccountsProviderPort;
import com.netpay.speiprovider.infrastructure.factory.AccountsProviderFactory;

@Service
public class GetAccountsUseCase implements GetAccountsPort {

    private final AccountsProviderFactory factory;

    public GetAccountsUseCase(AccountsProviderFactory factory) {
        this.factory = factory;
    }

    @Override
    public AccountsResult getAccounts(String clientId, String providerName) {
        AccountsProviderPort provider = factory.getProvider(providerName);
        return provider.retrieveAccounts(clientId);
    }
}