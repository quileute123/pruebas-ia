package com.netpay.speiprovider.infrastructure.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.domain.port.outbound.PayoutProviderPort;

/**
 * Resuelve el proveedor de dispersiones por nombre; si no se indica, usa el default configurado
 * en {@code spei.provider.default} (MONATO por defecto).
 */
@Component
public class PayoutProviderFactory {

    private final Map<String, PayoutProviderPort> providersMap;
    private final String defaultProvider;

    public PayoutProviderFactory(
            List<PayoutProviderPort> providers,
            @Value("${spei.provider.default:MONATO}") String defaultProvider) {

        this.providersMap = providers.stream()
                .collect(Collectors.toMap(
                        p -> p.getProviderName().toUpperCase(),
                        Function.identity()
                ));
        this.defaultProvider = defaultProvider.toUpperCase();
    }

    public PayoutProviderPort getProvider(String providerName) {
        String target = (providerName != null && !providerName.isBlank())
                ? providerName.toUpperCase()
                : defaultProvider;

        PayoutProviderPort provider = providersMap.get(target);
        if (provider == null) {
            throw new IllegalArgumentException("Provider not supported: " + target);
        }
        return provider;
    }
}