package com.netpay.speiprovider.infrastructure.adapter.inbound.scheduler;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.domain.port.inbound.SyncBanksPort;
import com.netpay.speiprovider.infrastructure.config.BanksSyncProperties;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Job semanal para mantener actualizado el catálogo de bancos Monato.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "spei.banks.sync", name = "enabled", havingValue = "true", matchIfMissing = true)
public class BanksCatalogSyncScheduler {

	private final SyncBanksPort syncBanksPort;
	private final BanksSyncProperties banksSyncProperties;

	@Scheduled(cron = "${spei.banks.sync.cron:0 0 2 * * SUN}")
	public void syncBanksCatalog() {
		log.info("Iniciando sync calendarizado de catálogo de bancos. provider={}", banksSyncProperties.getProvider());
		try {
			var result = syncBanksPort.syncBanks(banksSyncProperties.getProvider(), banksSyncProperties.getModifiedBy());
			log.info("Sync calendarizado de bancos finalizado. total={}", result.totalBanks());
		}
		catch (Exception ex) {
			log.error("Error en sync calendarizado de catálogo de bancos", ex);
		}
	}

}
