package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.BanksCatalogResult;

/**
 * Puerto de entrada: sincroniza el catálogo de bancos desde el proveedor SPEI y lo persiste.
 */
public interface SyncBanksPort {

	BanksCatalogResult syncBanks(String providerName, String modifiedBy);

}
