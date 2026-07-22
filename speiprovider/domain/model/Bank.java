package com.netpay.speiprovider.domain.model;

/**
 * Banco del catálogo Monato (participante SPEI).
 */
public record Bank(
		String id,
		String name,
		String token,
		String bim,
		String code,
		String bankStatus) {
}
