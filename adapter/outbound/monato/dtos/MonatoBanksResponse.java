package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

/**
 * Response de {@code GET /v1/banks} (catálogo paginado de participantes SPEI).
 */
@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonatoBanksResponse {

	@JsonProperty("total_banks")
	private Integer totalBanks;

	private Integer page;

	@JsonProperty("page_size")
	private Integer pageSize;

	private List<Bank> banks;

	@Getter
	@Builder
	@Jacksonized
	@JsonIgnoreProperties(ignoreUnknown = true)
	public static class Bank {

		private String id;

		private String name;

		/** Código de banco en la CLABE. */
		private String token;

		@JsonProperty("BIM")
		private String bim;

		private String code;

		@JsonProperty("bank_status")
		private String bankStatus;

	}

}
