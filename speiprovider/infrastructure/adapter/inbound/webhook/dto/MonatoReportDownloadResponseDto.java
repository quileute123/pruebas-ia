package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MonatoReportDownloadResponseDto {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("download_url")
    private String downloadUrl;
}