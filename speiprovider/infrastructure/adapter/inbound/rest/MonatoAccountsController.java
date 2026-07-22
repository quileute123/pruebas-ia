package com.netpay.speiprovider.infrastructure.adapter.inbound.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netpay.speiprovider.domain.port.inbound.GetAccountsPort;
import com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto.MonatoAccountsResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spei")
@RequiredArgsConstructor
public class MonatoAccountsController {

    private final GetAccountsPort getAccountsPort;

    @GetMapping("/accounts")
    public ResponseEntity<MonatoAccountsResponseDto> retrieveAccounts(
            @RequestParam(required = false) String clientId,
            @RequestHeader(value = "X-SPEI-Provider", defaultValue = "MONATO") String provider) {
        return ResponseEntity.ok(MonatoAccountsResponseDto
                .from(getAccountsPort.getAccounts(clientId, provider)));
    }

}