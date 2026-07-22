package com.netpay.speiprovider.infrastructure.adapter.outbound.persistence.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.netpay.speiprovider.infrastructure.adapter.outbound.persistence.entity.MonatoBankEntity;

public interface MonatoBankJpaRepository extends JpaRepository<MonatoBankEntity, String> {
}
