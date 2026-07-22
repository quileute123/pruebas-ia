package com.netpay.speiprovider.domain.port.inbound;

import com.netpay.speiprovider.domain.model.MoneyInEvent;

public interface MoneyInProcessingPort {
	/**
     * Procesa la entrada de dinero notificada por el proveedor (MONATO).
     */
    void process(MoneyInEvent event);
}
