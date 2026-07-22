package com.netpay.speiprovider.infrastructure.adapter.inbound.rest.handler;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import com.netpay.speiprovider.infrastructure.adapter.inbound.rest.error.ApiErrorResponse;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex,
            HttpServletRequest request) {
        log.warn("Argumento invalido: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_ARGUMENT", ex.getMessage(), request.getRequestURI(), null,
                null);
    }

    @ExceptionHandler(MonatoIntegrationException.class)
    public ResponseEntity<ApiErrorResponse> handleMonatoIntegrationException(MonatoIntegrationException ex,
            HttpServletRequest request) {
        log.error("Error Monato en {}: {}", ex.getOperation(), ex.getMessage(), ex);
        Integer upstream = ex.getUpstreamStatus() != null ? ex.getUpstreamStatus().value() : null;
        HttpStatus status = mapUpstreamStatus(upstream, HttpStatus.BAD_GATEWAY);
        return buildResponse(status, "MONATO_INTEGRATION_ERROR", ex.getMessage(), request.getRequestURI(),
                ApiErrorResponse.ProviderError.from(ex.getProviderError()), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidationException(MethodArgumentNotValidException ex,
            HttpServletRequest request) {
        var fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request invalido", request.getRequestURI(),
                null, fieldErrors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadableMessage(HttpMessageNotReadableException ex,
            HttpServletRequest request) {
        log.warn("Body invalido: {}", ex.getMessage());
        return buildResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY", "El body del request es invalido",
                request.getRequestURI(), null, null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFoundException(NoResourceFoundException ex,
            HttpServletRequest request) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return buildResponse(HttpStatus.NOT_FOUND, "NOT_FOUND", "Recurso no encontrado", request.getRequestURI(), null,
                null);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado", ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR",
                "Ocurrio un error inesperado en el servicio", request.getRequestURI(), null, null);
    }

    private HttpStatus mapUpstreamStatus(Integer upstream, HttpStatus defaultStatus) {
        if (upstream == null) {
            return defaultStatus;
        }
        if (upstream == 401 || upstream == 403) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (upstream == 422) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
        if (upstream >= 500) {
            return HttpStatus.BAD_GATEWAY;
        }
        if (upstream >= 400) {
            return HttpStatus.BAD_REQUEST;
        }
        return defaultStatus;
    }

    private ApiErrorResponse.FieldError toFieldError(FieldError fieldError) {
        return ApiErrorResponse.FieldError.of(fieldError.getField(), fieldError.getDefaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, String code, String message, String path,
            ApiErrorResponse.ProviderError provider, List<ApiErrorResponse.FieldError> fieldErrors) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(status, code, message, path, provider, fieldErrors));
    }

}