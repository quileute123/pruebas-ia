package com.netpay.speiprovider.infrastructure.adapter.outbound.persistence;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.netpay.speiprovider.domain.model.Bank;
import com.netpay.speiprovider.domain.model.BanksCatalogResult;
import com.netpay.speiprovider.domain.model.BankSyncStats;
import com.netpay.speiprovider.domain.port.outbound.BankCatalogRepositoryPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.persistence.entity.MonatoBankEntity;
import com.netpay.speiprovider.infrastructure.adapter.outbound.persistence.repository.MonatoBankJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class BankCatalogPersistenceAdapter implements BankCatalogRepositoryPort {

	private final MonatoBankJpaRepository repository;

	/**
	 * Sincroniza el catálogo local contra la lista de Monato:
	 * <ul>
	 *   <li>Si el banco no existe --> lo inserta.</li>
	 *   <li>Si existe y cambió name/token/BIM/code/bankStatus --> lo actualiza (fecha/usuario de control).</li>
	 *   <li>Si existe y no cambió --> no escribe en BD.</li>
	 * </ul>
	 * Devuelve conteos de insertados, actualizados y sin cambio.
	 */
	@Override
	public BankSyncStats upsertChanged(List<Bank> banks, String modifiedBy) {
		if (banks == null || banks.isEmpty()) {
			return new BankSyncStats(0, 0, 0);
		}

		var now = LocalDateTime.now();
		var ids = banks.stream().map(Bank::id).toList();
		var existingById = repository.findAllById(ids).stream().collect(Collectors.toMap(MonatoBankEntity::getBankId, Function.identity()));

		var toSave = new ArrayList<MonatoBankEntity>();
		int inserted = 0;
		int updated = 0;
		int unchanged = 0;

		for (Bank bank : banks) {
			var existing = existingById.get(bank.id());
			if (existing == null) {
				toSave.add(newEntity(bank, modifiedBy, now));
				inserted++;
				continue;
			}
			// Banco ya registrado: solo actualizar si name/token/BIM/code/bankStatus cambió desde el último sync.
			if (hasChanges(existing, bank)) {
				applyChanges(existing, bank, modifiedBy, now);
				toSave.add(existing);
				updated++;
			}
			else {
				// Sin cambios: no se escribe en BD.
				unchanged++;
			}
		}

		if (!toSave.isEmpty()) {
			repository.saveAll(toSave);
		}
		return new BankSyncStats(inserted, updated, unchanged);
	}

	@Override
	public List<Bank> findAll() {
		return repository.findAll().stream().map(this::toDomain).toList();
	}

	@Override
	public BanksCatalogResult findPage(int page, int pageSize) {
		var pageable = PageRequest.of(page - 1, pageSize, Sort.by("name").ascending());
		var result = repository.findAll(pageable);
		var banks = result.getContent().stream().map(this::toDomain).toList();
		return new BanksCatalogResult(banks, (int) result.getTotalElements(), page, pageSize);
	}

	private MonatoBankEntity newEntity(Bank bank, String modifiedBy, LocalDateTime now) {
		var entity = new MonatoBankEntity();
		entity.setBankId(bank.id());
		entity.setCreationDate(now);
		entity.setCreatedBy(modifiedBy);
		applyChanges(entity, bank, modifiedBy, now);
		return entity;
	}

	private void applyChanges(MonatoBankEntity entity, Bank bank, String modifiedBy, LocalDateTime now) {
		entity.setName(bank.name());
		entity.setToken(bank.token());
		entity.setBim(bank.bim());
		entity.setCode(bank.code());
		entity.setBankStatus(bank.bankStatus());
		entity.setUpdateDate(now);
		entity.setLastUpdateBy(modifiedBy);
	}

	private boolean hasChanges(MonatoBankEntity existing, Bank bank) {
		return !Objects.equals(existing.getName(), bank.name())
				|| !Objects.equals(existing.getToken(), bank.token())
				|| !Objects.equals(existing.getBim(), bank.bim())
				|| !Objects.equals(existing.getCode(), bank.code())
				|| !Objects.equals(existing.getBankStatus(), bank.bankStatus());
	}

	private Bank toDomain(MonatoBankEntity entity) {
		return new Bank(entity.getBankId(),
						entity.getName(),
						entity.getToken(),
						entity.getBim(),
						entity.getCode(),
						entity.getBankStatus());
	}

}
