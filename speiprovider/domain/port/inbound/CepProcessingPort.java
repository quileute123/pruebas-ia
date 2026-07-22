package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.CepEvent;

public interface CepProcessingPort {
    /**
     * Procesa las actualizaciones de estado de CEP (Penny Validation).
     */
    void process(CepEvent event);
}
