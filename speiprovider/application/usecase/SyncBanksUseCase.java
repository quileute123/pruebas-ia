package com.netpay.speiprovider.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netpay.speiprovider.domain.model.BanksCatalogResult;
import com.netpay.speiprovider.domain.port.inbound.SyncBanksPort;
import com.netpay.speiprovider.domain.port.outbound.BankCatalogRepositoryPort;
import com.netpay.speiprovider.infrastructure.factory.BanksProviderFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncBanksUseCase implements SyncBanksPort {

	private static final String DEFAULT_USER = "SYSTEM";

	private final BanksProviderFactory banksProviderFactory;
	private final BankCatalogRepositoryPort bankCatalogRepository;

	@Override
	@Transactional
	public BanksCatalogResult syncBanks(String providerName, String modifiedBy) {
		var provider = banksProviderFactory.getProvider(providerName);
		var catalog = provider.retrieveAllBanks();
		var user = (modifiedBy != null && !modifiedBy.isBlank()) ? modifiedBy : DEFAULT_USER;
		var stats = bankCatalogRepository.upsertChanged(catalog.banks(), user);
		log.info("Sincronización de bancos finalizada. proveedor={}, obtenidos={}, insertados={}, actualizados={}, sinCambio={}",
				provider.getProviderName(), catalog.banks().size(), stats.inserted(), stats.updated(), stats.unchanged());
		return new BanksCatalogResult(catalog.banks(), catalog.banks().size());
	}

}
