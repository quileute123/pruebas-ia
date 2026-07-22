package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonatoStatusUpdateWebhookDto {

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

        @JsonProperty("message_type")
        private String messageType;

        @JsonProperty("reason")
        private String reason;

        @JsonProperty("reason_description")
        private String reasonDescription;

        @NotBlank(message = "status es requerido")
        private String status;

        @JsonProperty("update_at")
        private String updateAt;
    }
}
