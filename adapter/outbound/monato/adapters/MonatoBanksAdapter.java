package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;

import com.netpay.speiprovider.domain.model.Bank;
import com.netpay.speiprovider.domain.model.BanksCatalogResult;
import com.netpay.speiprovider.domain.port.outbound.BanksProviderPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client.MonatoHttpClient;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos.MonatoBanksResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonatoBanksAdapter implements BanksProviderPort {

	private static final String OPERATION = "GET /v1/banks";
	private static final int DEFAULT_PAGE_SIZE = 50;

	private final MonatoHttpClient monatoHttpClient;
	private final MonatoErrorParser monatoErrorParser;

	@Override
	public BanksCatalogResult retrieveAllBanks() {
		log.info("Consultando catálogo de bancos Monato");
		try {
			var allBanks = new ArrayList<Bank>();
			int page = 1;
			int totalBanks = 0;

			do {
				var response = monatoHttpClient.getBanks(page, DEFAULT_PAGE_SIZE);
				if (response == null) {
					throw new MonatoIntegrationException(OPERATION, HttpStatus.BAD_GATEWAY,
							"Monato devolvió respuesta vacía en GET /banks", null, null);
				}

				if (page == 1) {
					totalBanks = response.getTotalBanks() != null ? response.getTotalBanks() : 0;
				}

				var pageBanks = toDomain(response);
				if (pageBanks.isEmpty()) {
					break;
				}
				allBanks.addAll(pageBanks);

				log.debug("Página {} de bancos Monato: {} registros (acumulado={}, total={})",
						page, pageBanks.size(), allBanks.size(), totalBanks);
				page++;
			}
			while (allBanks.size() < totalBanks);

			log.info("Catálogo Monato obtenido: {} bancos", allBanks.size());
			return new BanksCatalogResult(allBanks, allBanks.size());
		}
		catch (RestClientResponseException ex) {
			throw monatoErrorParser.toException(OPERATION, ex);
		}
	}

	@Override
	public String getProviderName() {
		return "MONATO";
	}

	private List<Bank> toDomain(MonatoBanksResponse response) {
		if (response.getBanks() == null || response.getBanks().isEmpty()) {
			return List.of();
		}
		return response.getBanks().stream()
				.map(bank -> new Bank(
						bank.getId(),
						bank.getName(),
						bank.getToken(),
						bank.getBim(),
						bank.getCode(),
						bank.getBankStatus()))
				.toList();
	}

}
