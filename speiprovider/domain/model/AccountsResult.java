package com.netpay.speiprovider.domain.model;

import java.util.List;
import java.util.Optional;

/**
 * Resultado de dominio de una consulta de cuentas. Expone la cuenta centralizadora
 * de forma neutral, sin acoplar la capa de dominio a los DTOs de Monato.
 */
public record AccountsResult(List<ProviderAccount> accounts) {

    private static final String CENTRALIZING_ACCOUNT = "CENTRALIZING_ACCOUNT";

    public AccountsResult {
        accounts = accounts != null ? List.copyOf(accounts) : List.of();
    }

    public Optional<ProviderAccount> centralizingAccount() {
        if (accounts.isEmpty()) {
            return Optional.empty();
        }
        return accounts.stream()
                .filter(account -> CENTRALIZING_ACCOUNT.equals(account.accountType()))
                .findFirst()
                .or(() -> Optional.of(accounts.getFirst()));
    }
}