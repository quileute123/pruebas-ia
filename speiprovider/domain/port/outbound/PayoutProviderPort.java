package com.netpay.speiprovider.domain.port.outbound;

import com.netpay.speiprovider.domain.model.PayoutResponse;
import com.netpay.speiprovider.domain.model.PayoutTransfer;

/**
 * Puerto de salida que implementa cada proveedor capaz de ejecutar dispersiones.
 */
public interface PayoutProviderPort {

    PayoutResponse executePayout(PayoutTransfer transfer);

    String getProviderName();
}