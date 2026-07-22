package com.netpay.speiprovider.application.usecase;

import com.netpay.speiprovider.domain.model.MoneyInEvent;
import com.netpay.speiprovider.domain.port.inbound.MoneyInProcessingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessMoneyInUseCase implements MoneyInProcessingPort {

    @Override
    public void process(MoneyInEvent event) {
        log.info("Iniciando procesamiento en capa de Dominio para el evento: {}", event.getEventId());
        
        // TODO: Validar idempotencia (¿Ya procesamos este eventId?)
        // TODO: Consultar base de datos para actualizar saldos
        // TODO: Llamar al puerto de salida para notificar conciliación
        
        log.info("Procesamiento finalizado exitosamente para el evento: {}", event.getEventId());
    }
}
