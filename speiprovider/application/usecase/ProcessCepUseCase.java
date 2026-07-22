package com.netpay.speiprovider.application.usecase;

import com.netpay.speiprovider.domain.model.CepEvent;
import com.netpay.speiprovider.domain.port.inbound.CepProcessingPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessCepUseCase implements CepProcessingPort {

    @Override
    public void process(CepEvent event) {
        log.info("Iniciando procesamiento en capa de Dominio para CEP. Evento ID: {}", event.getEventId());
        
        // TODO: Validar idempotencia
        // TODO: Actualizar estado de Penny Validation en base de datos
        
        log.info("Procesamiento CEP finalizado exitosamente. Status: {}", event.getStatus());
    }
}