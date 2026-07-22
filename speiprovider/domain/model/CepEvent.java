package com.netpay.speiprovider.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CepEvent {
    private String eventId;
    private String trackingKey;
    private String beneficiaryAccount;
    private String beneficiaryName;
    private String status;
}
