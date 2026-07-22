package com.netpay.speiprovider.domain.model;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class MoneyInEvent {
    private String eventId;
    private String subCategory;
    private BigDecimal amount;
    private String beneficiaryAccount;
    private String trackingKey;
    
}
