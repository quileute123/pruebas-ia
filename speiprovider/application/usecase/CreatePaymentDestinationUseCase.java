package com.netpay.speiprovider.application.usecase;

import org.springframework.stereotype.Service;

import com.netpay.speiprovider.domain.model.PaymentDestination;
import com.netpay.speiprovider.domain.model.PaymentDestinationResponse;
import com.netpay.speiprovider.domain.port.inbound.CreatePaymentDestinationPort;
import com.netpay.speiprovider.domain.port.outbound.PaymentDestinationProviderPort;
import com.netpay.speiprovider.infrastructure.factory.PaymentDestinationProviderFactory;

@Service
public class CreatePaymentDestinationUseCase implements CreatePaymentDestinationPort {

    private final PaymentDestinationProviderFactory factory;

    public CreatePaymentDestinationUseCase(PaymentDestinationProviderFactory factory) {
        this.factory = factory;
    }

    @Override
    public PaymentDestinationResponse create(PaymentDestination destination, String providerName) {
        PaymentDestinationProviderPort provider = factory.getProvider(providerName);
        return provider.createDestination(destination);
    }
}