package com.netpay.speiprovider.application.usecase;

import com.netpay.speiprovider.domain.model.StatusUpdateEvent;
import com.netpay.speiprovider.domain.port.inbound.StatusUpdateProcessingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessStatusUpdateUseCase implements StatusUpdateProcessingPort {

    @Override
    public void process(StatusUpdateEvent event) {
        log.info("Iniciando procesamiento en capa de Dominio para STATUS_UPDATE. Evento ID: {}", event.getEventId());
        
        // TODO: Validar idempotencia
        // TODO: Actualizar el estado de la transacción original en base de datos
        
        log.info("Procesamiento STATUS_UPDATE finalizado exitosamente para la clave de rastreo: {}", event.getTrackingKey());
    }
}
