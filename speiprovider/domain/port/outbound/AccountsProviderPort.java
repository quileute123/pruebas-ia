package com.netpay.speiprovider.domain.port.outbound;

import com.netpay.speiprovider.domain.model.AccountsResult;

/**
 * Puerto de salida que implementa cada proveedor capaz de consultar cuentas.
 */
public interface AccountsProviderPort {

    AccountsResult retrieveAccounts(String clientId);

    String getProviderName();
}