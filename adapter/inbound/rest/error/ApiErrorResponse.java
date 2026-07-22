package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.error;

import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;

import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorResponse;

public record ApiErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        ProviderError provider,
        List<FieldError> fieldErrors) {

    public static ApiErrorResponse of(HttpStatus httpStatus, String code, String message, String path,
            ProviderError provider, List<FieldError> fieldErrors) {
        return new ApiErrorResponse(
                Instant.now(),
                httpStatus.value(),
                httpStatus.getReasonPhrase(),
                code,
                message,
                path,
                provider,
                fieldErrors == null ? null : List.copyOf(fieldErrors));
    }

    public record ProviderError(
            String module,
            String method,
            String errorCode,
            String httpCode,
            String detail) {

        public static ProviderError from(MonatoErrorResponse errorResponse) {
            if (errorResponse == null) {
                return null;
            }

            var metadata = errorResponse.resolveMetadata();
            if (metadata == null) {
                return new ProviderError(null, null, null, null, errorResponse.getMessage());
            }

            return new ProviderError(
                    metadata.getModule(),
                    metadata.getMethodName(),
                    metadata.getErrorCode(),
                    metadata.getHttpCode(),
                    metadata.getErrorDetail());
        }

    }

    public record FieldError(String field, String message) {

        public static FieldError of(String field, String message) {
            return new FieldError(field, message);
        }

    }

}
