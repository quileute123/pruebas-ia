package com.netpay.speiprovider.domain.port.outbound;

import com.netpay.speiprovider.domain.model.PaymentDestination;
import com.netpay.speiprovider.domain.model.PaymentDestinationResponse;

/**
 * Puerto de salida que implementa cada proveedor capaz de registrar destinos de pago.
 */
public interface PaymentDestinationProviderPort {

    PaymentDestinationResponse createDestination(PaymentDestination destination);

    String getProviderName();
}