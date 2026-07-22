package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.netpay.speiprovider.domain.model.PaymentDestination;
import com.netpay.speiprovider.domain.model.PaymentDestinationResponse;
import com.netpay.speiprovider.domain.port.outbound.PaymentDestinationProviderPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client.MonatoHttpClient;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoInstrumentRequest;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class MonatoPaymentDestinationAdapter implements PaymentDestinationProviderPort {

    private static final String OPERATION = "POST /v1/clients/{clientId}/instruments";

    private final MonatoHttpClient monatoHttpClient;
    private final MonatoProperties monatoProperties;
    private final MonatoErrorParser monatoErrorParser;

    public MonatoPaymentDestinationAdapter(
            MonatoHttpClient monatoHttpClient,
            MonatoProperties monatoProperties,
            MonatoErrorParser monatoErrorParser) {
        this.monatoHttpClient = monatoHttpClient;
        this.monatoProperties = monatoProperties;
        this.monatoErrorParser = monatoErrorParser;
    }

    @Override
    public PaymentDestinationResponse createDestination(PaymentDestination destination) {

        String resolvedClientId = resolveClientId(destination.clientId());
        log.info("Creando instrumento de destino en Monato para clientId={}", resolvedClientId);

        var virtualClabe = new MonatoInstrumentRequest.VirtualClabeDto(
                destination.destinationBankId(),
                destination.accountNumber(),
                destination.clabeNumber(),
                destination.holderName()
        );

        var request = new MonatoInstrumentRequest(
                destination.sourceBankId(),
                resolvedClientId,
                destination.type(),
                destination.rfc(),
                destination.alias(),
                virtualClabe
        );

        try {
            var response = monatoHttpClient.createDestinationInstrument(resolvedClientId, request);

            if (response == null) {
                throw new MonatoIntegrationException(OPERATION, org.springframework.http.HttpStatusCode.valueOf(502),
                        "Monato devolvió respuesta vacía en la creación del instrumento", null, null);
            }

            String status = (response.audit() != null && "None".equals(response.audit().blockedAt()))
                    ? "ACTIVE"
                    : "PENDING";

            log.info("Instrumento creado exitosamente en Monato. instrumentId={}", response.id());

            return new PaymentDestinationResponse(response.id(), status);

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
