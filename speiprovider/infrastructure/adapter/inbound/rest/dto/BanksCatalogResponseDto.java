package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.dto;

import java.util.List;

import com.netpay.speiprovider.domain.model.Bank;
import com.netpay.speiprovider.domain.model.BanksCatalogResult;

public record BanksCatalogResponseDto(
		int totalBanks,
		int page,
		int pageSize,
		List<BankDto> banks) {

	public static BanksCatalogResponseDto from(BanksCatalogResult result) {
		var banks = result.banks().stream().map(BankDto::from).toList();
		return new BanksCatalogResponseDto(
				result.totalBanks(),
				result.page(),
				result.pageSize(),
				banks);
	}

	public record BankDto(
			String id,
			String name,
			String token,
			String bim,
			String code,
			String bankStatus) {

		public static BankDto from(Bank bank) {
			return new BankDto(
					bank.id(),
					bank.name(),
					bank.token(),
					bank.bim(),
					bank.code(),
					bank.bankStatus());
		}
	}

}
