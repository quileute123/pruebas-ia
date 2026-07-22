package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadRequestDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadResponseDto;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoAccountsResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoBanksResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoInstrumentRequest;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoInstrumentResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoMoneyOutRequest;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoMoneyOutResponse;

/**
 * Cliente declarativo hacia Monato. Spring genera el proxy a partir de un RestClient
 * (ver MonatoClientConfiguration). Base path /v1; cada operacion define su sub-path.
 */
@HttpExchange("/v1")
public interface MonatoHttpClient {

	@GetExchange("/clients/{clientId}/accounts")
	MonatoAccountsResponse getAccounts(@PathVariable("clientId") String clientId);

	@GetExchange("/banks")
	MonatoBanksResponse getBanks(
			@RequestParam(value = "page", required = false) Integer page,
			@RequestParam(value = "page_size", required = false) Integer pageSize);

	@PostExchange("/clients/{clientId}/instruments")
	MonatoInstrumentResponse createDestinationInstrument(
			@PathVariable("clientId") String clientId,
			@RequestBody MonatoInstrumentRequest request);

	@PostExchange("/transactions/money_out")
	MonatoMoneyOutResponse executeMoneyOut(
			@RequestHeader("Idempotency-Key") String idempotencyKey,
			@RequestBody MonatoMoneyOutRequest request);

	@PostExchange("/reports/clients/{clientId}/report/download")
	MonatoReportDownloadResponseDto fetchReportDownloadUrl(
			@PathVariable("clientId") String clientId,
			@RequestBody MonatoReportDownloadRequestDto request);
}
