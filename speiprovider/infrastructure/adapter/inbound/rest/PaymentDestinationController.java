package com.netpay.speiprovider.infrastructure.adapter.inbound.rest;

import com.netpay.speiprovider.domain.model.PaymentDestination;
import com.netpay.speiprovider.domain.model.PaymentDestinationResponse;
import com.netpay.speiprovider.domain.port.inbound.CreatePaymentDestinationPort;
import com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto.PaymentDestinationRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/spei/destinations")
@RequiredArgsConstructor
public class PaymentDestinationController {

    private final CreatePaymentDestinationPort createPaymentDestinationPort;

    @PostMapping
    public ResponseEntity<PaymentDestinationResponse> createDestination(
            @RequestHeader(value = "X-SPEI-Provider", required = false) String providerHeader,
            @RequestBody PaymentDestinationRequestDto dto) {

        PaymentDestination destination = dto.toDomain();

        PaymentDestinationResponse response = createPaymentDestinationPort.create(destination, providerHeader);
        return ResponseEntity.ok(response);
    }

}
