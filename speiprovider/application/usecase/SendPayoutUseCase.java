package com.netpay.speiprovider.application.usecase;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.netpay.speiprovider.domain.model.PayoutResponse;
import com.netpay.speiprovider.domain.model.PayoutTransfer;
import com.netpay.speiprovider.domain.port.inbound.PayoutProcessingPort;
import com.netpay.speiprovider.domain.port.outbound.PayoutProviderPort;
import com.netpay.speiprovider.infrastructure.factory.PayoutProviderFactory;

@Service
public class SendPayoutUseCase implements PayoutProcessingPort {

    private final PayoutProviderFactory providerFactory;

    public SendPayoutUseCase(PayoutProviderFactory providerFactory) {
        this.providerFactory = providerFactory;
    }

    @Override
    public PayoutResponse execute(PayoutTransfer transfer, String providerName) {
        if (transfer.amount() == null || transfer.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }

        PayoutProviderPort provider = providerFactory.getProvider(providerName);
        return provider.executePayout(transfer);
    }
}