package com.netpay.speiprovider.application.usecase;

import com.netpay.speiprovider.domain.model.ReportFileEvent;
import com.netpay.speiprovider.domain.port.inbound.ReportFileProcessingPort;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters.MonatoReportRestClientAdapter;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadRequestDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;

@Slf4j
@Service
public class ProcessReportFileUseCase implements ReportFileProcessingPort {

    private final MonatoReportRestClientAdapter monatoReportAdapter;

public ProcessReportFileUseCase(MonatoReportRestClientAdapter monatoReportAdapter) {
    this.monatoReportAdapter = monatoReportAdapter;
}

    @Override
    public void process(ReportFileEvent event) {
        String nombreProceso = "MonatoReportDownloadUseCase";

        // 1. REGLA CORPORATIVA: Log de Inicio en 1 sola línea (Begin)
        log.info("{}: Begin Task {} for clientId: {} and fileName: {}", 
                 nombreProceso, nombreProceso, event.getClientId(), event.getFileName());

        try {
            // PASO A: Generar llave de idempotencia dictada por la API para auditoría interna
            String idempotencyKey = event.getClientId() + "_" + event.getFileName();

            // PASO B: Preparar la fecha de operación (YYYY-MM-DD) y validar si viene cuenta o CLABE
            String operationDate = LocalDate.now().toString();
            String clabeOrAccount = event.getAccountId() != null ? event.getAccountId() : "N/A";

            MonatoReportDownloadRequestDto requestDto = new MonatoReportDownloadRequestDto();
            requestDto.setClabeNumber(clabeOrAccount);
            requestDto.setReportType(event.getFileType());
            requestDto.setOperationDate(operationDate);

            // PASO C: Ir al adaptador para solicitar la URL temporal de descarga del archivo
            MonatoReportDownloadResponseDto linkResponse = monatoReportAdapter.fetchReportDownloadUrl(
                    event.getClientId(), requestDto);

            if (linkResponse == null || linkResponse.getDownloadUrl() == null) {
                throw new RuntimeException("La API devolvió una URL vacía al solicitar el archivo: " + event.getFileName());
            }

            // PASO D: Descargar los bytes reales del archivo CSV desde la URL temporal
            byte[] csvBytes = monatoReportAdapter.downloadCsvFileBytes(linkResponse.getDownloadUrl());

            if (csvBytes == null || csvBytes.length == 0) {
                throw new RuntimeException("El archivo CSV descargado está vacío (0 bytes).");
            }

            // PASO E: Guardar físicamente en el servidor (Ruta temporal de trabajo)
            Path folderPath = Paths.get("/tmp/monato/reports/" + event.getClientId());
            if (!Files.exists(folderPath)) {
                Files.createDirectories(folderPath);
            }
            Path filePath = folderPath.resolve(event.getFileName());
            Files.write(filePath, csvBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

            // 2. REGLA CORPORATIVA: Log de Éxito en 1 sola línea (End successfully)
            log.info("{}: End task {} successfully for clientId: {} - File saved at {} (Size: {} bytes, IdempotencyKey: {})", 
                     nombreProceso, nombreProceso, event.getClientId(), filePath.toString(), csvBytes.length, idempotencyKey);

        } catch (Exception e) {
            // 3. REGLA CORPORATIVA: Log de Error en 1 sola línea (End with error)
            log.error("{}: End task {} with error error_description: {} - ClientId: {}", 
                      nombreProceso, nombreProceso, e.getMessage(), event.getClientId(), e);

            // Relanzamos la excepción para el manejo de errores global del microservicio
            throw new RuntimeException("Fallo crítico en el procesamiento y descarga del reporte de Monato", e);
        }
    }
}