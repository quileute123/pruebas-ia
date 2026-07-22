package com.netpay.speiprovider.infrastructure.adapter.inbound.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.netpay.speiprovider.domain.port.inbound.GetBanksPort;
import com.netpay.speiprovider.domain.port.inbound.SyncBanksPort;
import com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto.BanksCatalogResponseDto;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/spei/banks")
@RequiredArgsConstructor
public class BanksController {

	private final SyncBanksPort syncBanksPort;
	private final GetBanksPort getBanksPort;

	/**
	 * Sincroniza el catálogo desde Monato ({@code GET /v1/banks}) y lo persiste localmente.
	 */
	@PostMapping("/sync")
	public ResponseEntity<BanksCatalogResponseDto> syncBanks(
			@RequestHeader(value = "X-SPEI-Provider", defaultValue = "MONATO") String provider,
			@RequestHeader(value = "X-User-Id", required = false) String userId) {
		return ResponseEntity.ok(BanksCatalogResponseDto.from(syncBanksPort.syncBanks(provider, userId)));
	}

	/**
	 * Consulta el catálogo local paginado (page 1-based).
	 */
	@GetMapping
	public ResponseEntity<BanksCatalogResponseDto> getBanks(@RequestParam(defaultValue = "1") int page, @RequestParam(name = "page_size", defaultValue = "50") int pageSize) {
		return ResponseEntity.ok(BanksCatalogResponseDto.from(getBanksPort.getBanks(page, pageSize)));
	}

}
