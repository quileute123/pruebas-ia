package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonatoCepWebhookDto {

    @NotBlank(message = "id_msg es requerido")
    @JsonProperty("id_msg")
    private String idMsg;

    @NotBlank(message = "msg_name es requerido")
    @JsonProperty("msg_name")
    private String msgName;

    @JsonProperty("msg_date")
    private String msgDate;

    @Valid
    @NotNull(message = "body es requerido")
    private Body body;

    @Data
    public static class Body {
        @NotBlank(message = "id del body es requerido")
        private String id;

        @JsonProperty("tracking_key")
        private String trackingKey;

        @JsonProperty("beneficiary_account")
        private String beneficiaryAccount;

        @JsonProperty("beneficiary_name")
        private String beneficiaryName;

        @JsonProperty("beneficiary_rfc")
        private String beneficiaryRfc;

        @NotBlank(message = "status es requerido")
        private String status;

        @JsonProperty("processed_at")
        private String processedAt;
    }
}
