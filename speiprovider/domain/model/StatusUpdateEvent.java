package com.netpay.speiprovider.domain.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StatusUpdateEvent {
    private String eventId;
    private String trackingKey;
    private String messageType;
    private String status;
    private String reason;
    private String reasonDescription;
}
