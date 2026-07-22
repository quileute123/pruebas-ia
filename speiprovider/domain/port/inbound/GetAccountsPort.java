package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.AccountsResult;

/**
 * Puerto de entrada para consultar cuentas del proveedor SPEI.
 */
public interface GetAccountsPort {

    AccountsResult getAccounts(String clientId, String providerName);
}