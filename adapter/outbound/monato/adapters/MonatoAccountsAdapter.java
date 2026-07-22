package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.netpay.speiprovider.domain.model.AccountsResult;
import com.netpay.speiprovider.domain.model.ProviderAccount;
import com.netpay.speiprovider.domain.port.outbound.AccountsProviderPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client.MonatoHttpClient;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoAccountsResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MonatoAccountsAdapter implements AccountsProviderPort {

    private static final String OPERATION = "GET /v1/clients/{clientId}/accounts";

    private final MonatoHttpClient monatoHttpClient;
    private final MonatoProperties monatoProperties;
    private final MonatoErrorParser monatoErrorParser;

    public MonatoAccountsAdapter(
            MonatoHttpClient monatoHttpClient,
            MonatoProperties monatoProperties,
            MonatoErrorParser monatoErrorParser) {
        this.monatoHttpClient = monatoHttpClient;
        this.monatoProperties = monatoProperties;
        this.monatoErrorParser = monatoErrorParser;
    }

    @Override
    public AccountsResult retrieveAccounts(String clientId) {
        var resolvedClientId = resolveClientId(clientId);
        log.info("Consultando cuentas Monato para clientId={}", resolvedClientId);

        try {
            var response = monatoHttpClient.getAccounts(resolvedClientId);

            if (response == null) {
                throw new MonatoIntegrationException(OPERATION, org.springframework.http.HttpStatusCode.valueOf(502),
                        "Monato devolvió respuesta vacía en GET /accounts", null, null);
            }

            var result = toDomain(response);
            result.centralizingAccount()
                    .ifPresent(account -> log.info("Cuenta centralizadora encontrada. instrumentId={}",
                            account.instrumentId()));

            return result;
        }
        catch (RestClientResponseException ex) {
            throw monatoErrorParser.toException(OPERATION, ex);
        }
    }

    @Override
    public String getProviderName() {
        return "MONATO";
    }

    private AccountsResult toDomain(MonatoAccountsResponse response) {
        var accounts = response.getData() == null ? List.<ProviderAccount>of()
                : response.getData().stream()
                        .map(account -> new ProviderAccount(account.getId(), account.getAccountType(),
                                account.getInstrumentId()))
                        .toList();
        return new AccountsResult(accounts);
    }

    private String resolveClientId(String clientId) {
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }
        if (monatoProperties.getClientId() == null || monatoProperties.getClientId().isBlank()) {
            throw new IllegalArgumentException(
                    "client-id no configurado en monato.client-id ni enviado como query param clientId");
        }
        return monatoProperties.getClientId();
    }
}
