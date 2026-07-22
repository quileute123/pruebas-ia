package com.netpay.speiprovider.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportFileEvent {
    private String clientId;
    private String fileType;
    private String period;
    private String fileName;
    private String accountId; // Puede ser nulo para reportes diarios
}
