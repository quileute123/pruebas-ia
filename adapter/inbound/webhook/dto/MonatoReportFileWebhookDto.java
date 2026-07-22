package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MonatoReportFileWebhookDto {

    @NotBlank(message = "client_id es requerido")
    @JsonProperty("client_id")
    private String clientId;

    @NotBlank(message = "file_type es requerido")
    @JsonProperty("file_type")
    private String fileType;

    @NotBlank(message = "period es requerido")
    private String period;

    @NotBlank(message = "file_name es requerido")
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("created_at")
    private String createdAt;

    // Este es opcional (no lleva @NotBlank) porque no viene en los reportes DAILY
    @JsonProperty("account_id")
    private String accountId;
}
