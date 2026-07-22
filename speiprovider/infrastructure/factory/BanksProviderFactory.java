package com.netpay.speiprovider.infrastructure.factory;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.domain.port.outbound.BanksProviderPort;

/**
 * Resuelve el proveedor de catálogo de bancos; default {@code spei.provider.default}.
 */
@Component
public class BanksProviderFactory {

	private final Map<String, BanksProviderPort> providersMap;
	private final String defaultProvider;

	public BanksProviderFactory(List<BanksProviderPort> providers,
			@Value("${spei.provider.default:MONATO}") String defaultProvider) {
		this.providersMap = providers.stream()
				.collect(Collectors.toMap(
						provider -> provider.getProviderName().toUpperCase(),
						Function.identity()));
		this.defaultProvider = defaultProvider.toUpperCase();
	}

	public BanksProviderPort getProvider(String providerName) {
		var targetProvider = (providerName != null && !providerName.isBlank())
				? providerName.toUpperCase()
				: defaultProvider;
		var provider = providersMap.get(targetProvider);
		if (provider == null) {
			throw new IllegalArgumentException(
					"Proveedor de bancos no registrado o no soportado: " + targetProvider);
		}
		return provider;
	}

}
