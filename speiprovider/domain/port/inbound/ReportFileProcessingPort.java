package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.ReportFileEvent;

public interface ReportFileProcessingPort {
    /**
     * Procesa las notificaciones de disponibilidad de archivos de reportes.
     */
    void process(ReportFileEvent event);
}
