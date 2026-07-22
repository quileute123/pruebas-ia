package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.PaymentDestination;
import com.netpay.speiprovider.domain.model.PaymentDestinationResponse;

public interface CreatePaymentDestinationPort {
    PaymentDestinationResponse create(PaymentDestination destination, String providerHeader);
}
