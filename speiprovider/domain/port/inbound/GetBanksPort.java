package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.BanksCatalogResult;

/**
 * Puerto de entrada para consultar el catálogo de bancos persistido localmente.
 */
public interface GetBanksPort {

	/** Catálogo completo (sin paginar). */
	BanksCatalogResult getBanks();

	/** Catálogo paginado (page 1-based). */
	BanksCatalogResult getBanks(int page, int pageSize);

}
