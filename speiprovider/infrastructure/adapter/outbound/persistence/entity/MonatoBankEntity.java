package com.netpay.speiprovider.infrastructure.adapter.outbound.persistence.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "NS_MONATO_BANKS", schema = "NS_NETPAY_CONC")
public class MonatoBankEntity {

	@Id
	@Column(name = "BANK_ID", length = 36, nullable = false)
	private String bankId;

	@Column(name = "NAME", length = 200, nullable = false)
	private String name;

	@Column(name = "TOKEN", length = 10)
	private String token;

	@Column(name = "BIM", length = 10)
	private String bim;

	@Column(name = "CODE", length = 20)
	private String code;

	@Column(name = "BANK_STATUS", length = 20)
	private String bankStatus;

	@Column(name = "CREATION_DATE", nullable = false)
	private LocalDateTime creationDate;

	@Column(name = "CREATED_BY", length = 100)
	private String createdBy;

	@Column(name = "UPDATE_DATE", nullable = false)
	private LocalDateTime updateDate;

	@Column(name = "LAST_UPDATE_BY", length = 100)
	private String lastUpdateBy;

}
