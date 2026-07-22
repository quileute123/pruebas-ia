package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.netpay.speiprovider.domain.model.CepEvent;
import com.netpay.speiprovider.domain.model.MoneyInEvent;
import com.netpay.speiprovider.domain.model.ReportFileEvent;
import com.netpay.speiprovider.domain.model.StatusUpdateEvent;
import com.netpay.speiprovider.domain.port.inbound.CepProcessingPort;
import com.netpay.speiprovider.domain.port.inbound.MoneyInProcessingPort;
import com.netpay.speiprovider.domain.port.inbound.ReportFileProcessingPort;
import com.netpay.speiprovider.domain.port.inbound.StatusUpdateProcessingPort;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoCepWebhookDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoMoneyInWebhookDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportFileWebhookDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoStatusUpdateWebhookDto;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("${monato.paths.base-webhook:/v1/webhooks/monato}")
@RequiredArgsConstructor
public class MonatoWebhookController {

    private final MonatoProperties monatoProperties;
    
    // Inyectamos la Interfaz (El Puerto de Entrada)
    private final MoneyInProcessingPort moneyInProcessingPort;
    private final StatusUpdateProcessingPort statusUpdateProcessingPort;
    private final CepProcessingPort cepProcessingPort;
    private final ReportFileProcessingPort reportFileProcessingPort;

    @PostMapping("${monato.paths.money-in:/money-in}")
    public ResponseEntity<Void> receiveMoneyIn(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MonatoMoneyInWebhookDto requestBody) {
    	
        // 1. Validar Header
        // 2. Validar Firma (HMAC)
        log.info("Recibiendo webhook MONEY_IN. ID Msg: {}", requestBody.getIdMsg());
        if (!isSecurityValid(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        // 3. Mapear DTO a Modelo de Dominio
        MoneyInEvent domainEvent = MoneyInEvent.builder()
                .eventId(requestBody.getIdMsg())
                .subCategory(requestBody.getBody().getSubCategory())
                .amount(requestBody.getBody().getAmount())
                .beneficiaryAccount(requestBody.getBody().getBeneficiaryAccount())
                .trackingKey(requestBody.getBody().getTrackingKey())
                .build();

        // 4. Pasar a la capa de Dominio
        moneyInProcessingPort.process(domainEvent);

        log.info("Webhook MONEY_IN procesado exitosamente en capa de aplicación");

        // 4. Responder rápido
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("${monato.paths.status-update:/status-update}")
    public ResponseEntity<Void> receiveStatusUpdate(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MonatoStatusUpdateWebhookDto requestBody) {

        log.info("Recibiendo webhook STATUS_UPDATE. ID Msg: {}", requestBody.getIdMsg());
        if (!isSecurityValid(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        StatusUpdateEvent domainEvent = StatusUpdateEvent.builder()
                .eventId(requestBody.getIdMsg())
                .trackingKey(requestBody.getBody().getTrackingKey())
                .messageType(requestBody.getBody().getMessageType())
                .status(requestBody.getBody().getStatus())
                .reason(requestBody.getBody().getReason())
                .reasonDescription(requestBody.getBody().getReasonDescription())
                .build();

        statusUpdateProcessingPort.process(domainEvent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("${monato.paths.cep:/cep}")
    public ResponseEntity<Void> receiveCep(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MonatoCepWebhookDto requestBody) {

        log.info("Recibiendo webhook CEP. ID Msg: {}", requestBody.getIdMsg());
        if (!isSecurityValid(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        CepEvent domainEvent = CepEvent.builder()
                .eventId(requestBody.getIdMsg())
                .trackingKey(requestBody.getBody().getTrackingKey())
                .beneficiaryAccount(requestBody.getBody().getBeneficiaryAccount())
                .beneficiaryName(requestBody.getBody().getBeneficiaryName())
                .status(requestBody.getBody().getStatus())
                .build();

        cepProcessingPort.process(domainEvent);
        return ResponseEntity.ok().build();
    }

    @PostMapping("${monato.paths.reports:/reports}")
    public ResponseEntity<Void> receiveReports(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Valid @RequestBody MonatoReportFileWebhookDto requestBody) {

        log.info("Recibiendo webhook REPORTS. Archivo: {}", requestBody.getFileName());
        if (!isSecurityValid(authHeader)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        ReportFileEvent domainEvent = ReportFileEvent.builder()
                .clientId(requestBody.getClientId())
                .fileType(requestBody.getFileType())
                .period(requestBody.getPeriod())
                .fileName(requestBody.getFileName())
                .accountId(requestBody.getAccountId())
                .build();

        reportFileProcessingPort.process(domainEvent);
        return ResponseEntity.ok().build();
    }

    // --- Método Auxiliar de Seguridad ---
    private boolean isSecurityValid(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Webhook rechazado: Header Authorization ausente o inválido");
            return false;
        }
        
        try {
            String token = authHeader.substring(7);
            Algorithm algorithm = Algorithm.HMAC256(monatoProperties.getClientSecret());
            JWTVerifier verifier = JWT.require(algorithm).build();
            verifier.verify(token); 
            return true;
        } catch (JWTVerificationException exception) {
            log.error("Webhook rechazado: Firma de token inválida", exception);
            return false;
        }
    }
}
