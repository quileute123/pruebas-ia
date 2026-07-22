package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.dtos;

import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class MonatoAccountsResponse {

    private static final String CENTRALIZING_ACCOUNT = "CENTRALIZING_ACCOUNT";

    private List<Account> data;

    public Optional<Account> findCentralizingAccount() {
        if (data == null || data.isEmpty()) {
            return Optional.empty();
        }

        return data.stream()
                .filter(account -> CENTRALIZING_ACCOUNT.equals(account.getAccountType()))
                .findFirst()
                .or(() -> Optional.of(data.getFirst()));
    }

    @Getter
    @Builder
    @Jacksonized
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Account {

        private String id;

        @JsonProperty("accountType")
        private String accountType;

        @JsonProperty("instrumentId")
        private String instrumentId;

    }

}