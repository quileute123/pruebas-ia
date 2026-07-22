package com.netpay.speiprovider.domain.model;

/**
 * Resultado de una sincronización de catálogo (insertados / actualizados / sin cambios).
 */
public record BankSyncStats(int inserted, int updated, int unchanged) {

	public int written() {
		return inserted + updated;
	}

	public int totalProcessed() {
		return inserted + updated + unchanged;
	}

}
