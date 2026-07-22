package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.netpay.speiprovider.domain.model.PayoutResponse;
import com.netpay.speiprovider.domain.model.PayoutTransfer;
import com.netpay.speiprovider.domain.port.outbound.PayoutProviderPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client.MonatoHttpClient;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoMoneyOutRequest;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;
import com.netpay.speiprovider.infrastructure.util.IdempotencyKeyUtil;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MonatoPayoutAdapter implements PayoutProviderPort {

    private static final String OPERATION = "POST /v1/transactions/money_out";
    private static final String METHOD_MONEY_OUT = "money_out";

    private final MonatoHttpClient monatoHttpClient;
    private final MonatoProperties monatoProperties;
    private final MonatoErrorParser monatoErrorParser;
    private final String sourceInstrumentId;
    private final UUID namespaceUuid;

    public MonatoPayoutAdapter(
            MonatoHttpClient monatoHttpClient,
            MonatoProperties monatoProperties,
            MonatoErrorParser monatoErrorParser,
            @Value("${monato.source-instrument-id}") String sourceInstrumentId,
            @Value("${monato.idempotency.namespace}") UUID namespaceUuid) {
        this.monatoHttpClient = monatoHttpClient;
        this.monatoProperties = monatoProperties;
        this.monatoErrorParser = monatoErrorParser;
        this.sourceInstrumentId = sourceInstrumentId;
        this.namespaceUuid = namespaceUuid;
    }

    @Override
    public PayoutResponse executePayout(PayoutTransfer transfer) {

        String resolvedClientId = resolveClientId(transfer.clientId());
        log.info("Iniciando dispersión Money Out en Monato para clientId={}, destinationId={}",
                resolvedClientId, transfer.paymentDestinationId());

        var transactionRequest = new MonatoMoneyOutRequest.TransactionRequestDto(
                transfer.reference(),
                transfer.concept(),
                transfer.amount().toPlainString(),
                transfer.currency()
        );

        var moneyOutRequest = new MonatoMoneyOutRequest(
                resolvedClientId,
                this.sourceInstrumentId,
                transfer.paymentDestinationId(),
                transactionRequest
        );

        String idempotencyKey = IdempotencyKeyUtil.generate(
                namespaceUuid,
                resolvedClientId,
                METHOD_MONEY_OUT,
                moneyOutRequest
        );

        log.info("Ejecutando Money Out. ClientId={}, Idempotency-Key={}", resolvedClientId, idempotencyKey);

        try {
            var response = monatoHttpClient.executeMoneyOut(idempotencyKey, moneyOutRequest);

            if (response == null) {
                throw new MonatoIntegrationException(OPERATION, org.springframework.http.HttpStatusCode.valueOf(502),
                        "Monato devolvió respuesta vacía en POST /transactions/money_out", null, null);
            }

            log.info("Dispersión efectuada. transactionId={}, trackingId={}, externalReference={}, status={}",
                    response.id(), response.trackingId(), response.externalReference(), response.transactionStatus());

            return new PayoutResponse(
                    response.id(),
                    response.trackingId(),
                    response.externalReference(),
                    response.transactionStatus()
            );

        } catch (RestClientResponseException ex) {
            throw monatoErrorParser.toException(OPERATION, ex);
        }
    }

    @Override
    public String getProviderName() {
        return "MONATO";
    }

    private String resolveClientId(String clientId) {
        if (clientId != null && !clientId.isBlank()) {
            return clientId;
        }
        if (monatoProperties.getClientId() == null || monatoProperties.getClientId().isBlank()) {
            throw new IllegalArgumentException("client-id no configurado en monato.client-id ni enviado en la petición");
        }
        return monatoProperties.getClientId();
    }
}
