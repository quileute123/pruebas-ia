package com.netpay.speiprovider.domain.model;

import java.util.List;

/**
 * Resultado de sincronización / consulta del catálogo de bancos.
 */
public record BanksCatalogResult(List<Bank> banks, int totalBanks, int page, int pageSize) {

	public BanksCatalogResult(List<Bank> banks, int totalBanks) {
		this(banks, totalBanks, 1, banks != null ? banks.size() : 0);
	}

	public BanksCatalogResult {
		banks = banks != null ? List.copyOf(banks) : List.of();
	}

}
