package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MonatoReportDownloadRequestDto {
    @NotBlank
    @JsonProperty("clabe_number")
    private String clabeNumber;

    @NotBlank
    @JsonProperty("report_type")
    private String reportType;

    @NotBlank
    @JsonProperty("operation_date")
    private String operationDate;
}