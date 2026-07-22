package com.netpay.speiprovider.infrastructure.adapter.inbound.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.netpay.speiprovider.domain.model.PayoutResponse;
import com.netpay.speiprovider.domain.model.PayoutTransfer;
import com.netpay.speiprovider.domain.port.inbound.PayoutProcessingPort;
import com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto.PayoutRequestDto;

@RestController
@RequestMapping("/api/v1/spei")
public class PayoutController {

    private final PayoutProcessingPort payoutProcessingPort;

    public PayoutController(PayoutProcessingPort payoutProcessingPort) {
        this.payoutProcessingPort = payoutProcessingPort;
    }

    @PostMapping("/payout")
    public ResponseEntity<PayoutResponse> handlePayout(
            @RequestBody PayoutRequestDto dto,
            @RequestHeader(value = "X-SPEI-Provider", defaultValue = "MONATO") String provider) {

        PayoutTransfer transfer = new PayoutTransfer(
                dto.clientId(),
                dto.destinationId(),
                dto.amount(),
                dto.reference(),
                dto.concept(),
                dto.currency());

        return ResponseEntity.ok(payoutProcessingPort.execute(transfer, provider));
    }
}