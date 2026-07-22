package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.PayoutResponse;
import com.netpay.speiprovider.domain.model.PayoutTransfer;

/**
 * Puerto de entrada para procesar una dispersion (payout / money-out).
 */
public interface PayoutProcessingPort {

    PayoutResponse execute(PayoutTransfer transfer, String providerName);
}