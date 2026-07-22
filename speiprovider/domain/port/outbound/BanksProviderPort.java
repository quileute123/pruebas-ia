package com.netpay.speiprovider.domain.port.outbound;

import com.netpay.speiprovider.domain.model.BanksCatalogResult;

/**
 * Puerto de salida para obtener el catálogo de bancos del proveedor SPEI.
 */
public interface BanksProviderPort {

	BanksCatalogResult retrieveAllBanks();

	String getProviderName();

}
