package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.StatusUpdateEvent;

public interface StatusUpdateProcessingPort {
    /**
     * Procesa las actualizaciones de estado (rechazos/cancelaciones) de Money Outs.
     */
    void process(StatusUpdateEvent event);
}
