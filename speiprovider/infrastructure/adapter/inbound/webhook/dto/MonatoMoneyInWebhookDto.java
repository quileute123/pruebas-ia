package com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoMoneyInWebhookDto.Body;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MonatoMoneyInWebhookDto {
	
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

        @JsonProperty("beneficiary_account")
        private String beneficiaryAccount;

        @JsonProperty("beneficiary_name")
        private String beneficiaryName;

        @JsonProperty("beneficiary_rfc")
        private String beneficiaryRfc;

        @JsonProperty("payer_account")
        private String payerAccount;

        @JsonProperty("payer_name")
        private String payerName;

        @JsonProperty("payer_rfc")
        private String payerRfc;

        @JsonProperty("payer_institution")
        private String payerInstitution;

        // Jackson convierte inteligentemente el "123.00" de String a BigDecimal
        @NotNull(message = "amount es requerido")
        private BigDecimal amount;

        @JsonProperty("transaction_date")
        private String transactionDate;

        @JsonProperty("tracking_key")
        private String trackingKey;

        @JsonProperty("payment_concept")
        private String paymentConcept;

        @JsonProperty("numeric_reference")
        private String numericReference;

        @NotBlank(message = "sub_category es requerido")
        @JsonProperty("sub_category")
        private String subCategory;

        @JsonProperty("registered_at")
        private String registeredAt;

        @JsonProperty("owner_id")
        private String ownerId;
    }
	
}
