package com.netpay.speiprovider.application.usecase;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.netpay.speiprovider.domain.model.BanksCatalogResult;
import com.netpay.speiprovider.domain.port.inbound.GetBanksPort;
import com.netpay.speiprovider.domain.port.outbound.BankCatalogRepositoryPort;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GetBanksUseCase implements GetBanksPort {

	private static final int DEFAULT_PAGE = 1;
	private static final int DEFAULT_PAGE_SIZE = 50;
	private static final int MAX_PAGE_SIZE = 200;

	private final BankCatalogRepositoryPort bankCatalogRepository;

	@Override
	@Transactional(readOnly = true)
	public BanksCatalogResult getBanks() {
		var banks = bankCatalogRepository.findAll();
		return new BanksCatalogResult(banks, banks.size());
	}

	@Override
	@Transactional(readOnly = true)
	public BanksCatalogResult getBanks(int page, int pageSize) {
		int safePage = page < 1 ? DEFAULT_PAGE : page;
		int safeSize = pageSize < 1 ? DEFAULT_PAGE_SIZE : Math.min(pageSize, MAX_PAGE_SIZE);
		return bankCatalogRepository.findPage(safePage, safeSize);
	}

}
