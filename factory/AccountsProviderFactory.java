package com.netpay.speiprovider.infrastructure.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.domain.port.outbound.AccountsProviderPort;

/**
 * Resuelve el proveedor de cuentas por nombre; si no se indica, usa el default configurado
 * en {@code spei.provider.default} (MONATO por defecto).
 */
@Component
public class AccountsProviderFactory {

    private final Map<String, AccountsProviderPort> providersMap;
    private final String defaultProvider;

    public AccountsProviderFactory(List<AccountsProviderPort> providers,
            @Value("${spei.provider.default:MONATO}") String defaultProvider) {
        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        provider -> provider.getProviderName().toUpperCase(),
                        Function.identity()));
        this.defaultProvider = defaultProvider.toUpperCase();
    }

    public AccountsProviderPort getProvider(String providerName) {
        var targetProvider = (providerName != null && !providerName.isBlank())
                ? providerName.toUpperCase()
                : defaultProvider;
        var provider = providersMap.get(targetProvider);
        if (provider == null) {
            throw new IllegalArgumentException("Proveedor de cuentas no registrado o no soportado: " + targetProvider);
        }
        return provider;
    }
}