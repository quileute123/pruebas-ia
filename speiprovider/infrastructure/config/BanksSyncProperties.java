package com.netpay.speiprovider.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

/**
 * Calendarización del sync de catálogo de bancos Monato.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "spei.banks.sync")
public class BanksSyncProperties {

	/** Si false, no se registra el job calendarizado. */
	private boolean enabled = true;

	/** Proveedor SPEI a sincronizar. */
	private String provider = "MONATO";

	/**
	 * Cron semanal (default: domingo 02:00).
	 * Formato Spring: segundo minuto hora día-mes mes día-semana.
	 */
	private String cron = "0 0 2 * * SUN";

	/** Usuario de auditoría para ejecuciones calendarizadas. */
	private String modifiedBy = "SCHEDULER";

}
