package com.netpay.speiprovider.domain.port.outbound;

import java.util.List;

import com.netpay.speiprovider.domain.model.Bank;
import com.netpay.speiprovider.domain.model.BanksCatalogResult;
import com.netpay.speiprovider.domain.model.BankSyncStats;

public interface BankCatalogRepositoryPort {

	BankSyncStats upsertChanged(List<Bank> banks, String modifiedBy);

	List<Bank> findAll();

	BanksCatalogResult findPage(int page, int pageSize);

}
