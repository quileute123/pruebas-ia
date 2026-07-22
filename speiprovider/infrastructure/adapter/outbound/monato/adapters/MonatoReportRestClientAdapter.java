package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.adapters;

import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadRequestDto;
import com.netpay.speiprovider.infrastructure.adapter.inbound.webhook.dto.MonatoReportDownloadResponseDto;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoErrorParser;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.exception.MonatoIntegrationException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

// IMPORTS ADICIONALES PARA LA SIMULACIÓN LOCAL EN DISCO
import java.nio.file.Files;
import java.nio.file.Paths;

@Component
public class MonatoReportRestClientAdapter {

    private static final String DOWNLOAD_OPERATION = "POST /v1/reports/clients/{client_id}/report/download";
    private static final String FILE_OPERATION = "GET /report/file/download";

    //private final RestClient monatoRestClient;
    private final MonatoErrorParser monatoErrorParser;

    public MonatoReportRestClientAdapter(
            //@Qualifier("monatoRestClient") RestClient monatoRestClient,
            MonatoErrorParser monatoErrorParser) {
        //this.monatoRestClient = monatoRestClient;
        this.monatoErrorParser = monatoErrorParser;
    }

    /**
     * Consume el endpoint de Monato para obtener la URL temporal de descarga del reporte.
     */
    public MonatoReportDownloadResponseDto fetchReportDownloadUrl(String clientId, MonatoReportDownloadRequestDto requestDto) {
        // --- CÓDIGO REAL COMENTADO PARA SIMULACIÓN EN LOCAL ---
        try {
            // Nota técnica: Al igual que tu compañero, el monatoRestClient ya incluye la URL base compartida
            //MonatoReportDownloadResponseDto response = monatoRestClient.post()
                    //.uri("/v1/reports/clients/" + clientId + "/report/download")
                    //.body(requestDto)
                    //.retrieve()
                    //.body(MonatoReportDownloadResponseDto.class);

            //if (response == null) {
                //throw new MonatoIntegrationException(DOWNLOAD_OPERATION, org.springframework.http.HttpStatusCode.valueOf(502),
                        //"Monato devolvió respuesta vacía al solicitar URL de descarga", null, null);
            //}

            return null; //response;

        } catch (RestClientResponseException ex) {
            throw monatoErrorParser.toException(DOWNLOAD_OPERATION, ex);
        }

        // --- SIMULACIÓN LOCAL: Usamos constructor vacío y setters para evitar errores de Lombok ---
        // MonatoReportDownloadResponseDto response = new MonatoReportDownloadResponseDto();
        // response.setFileName("medidas.csv");
        // response.setDownloadUrl("https://simulacion-local.netpay/medidas.csv");
        
        // return response;
    }

    /**
     * Consume la URL temporal de descarga para obtener los bytes puros del CSV.
     */
    public byte[] downloadCsvFileBytes(String downloadUrl) {
        // --- CÓDIGO REAL COMENTADO PARA SIMULACIÓN EN LOCAL ---
        try {
            // Aquí usamos un RestClient alternativo o directo dado que la downloadUrl suele ser absoluta y temporal
            RestClient absoluteClient = RestClient.create();
            return absoluteClient.get()
                    .uri(downloadUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (RestClientResponseException ex) {
            throw monatoErrorParser.toException(FILE_OPERATION, ex);
        }

        // --- SIMULACIÓN LOCAL: Leemos el archivo real de tu carpeta de Descargas en Windows ---

        // try {
        //     // Usamos barras normales (/) para que Java no se confunda con el carácter de escape
        //     String rutaArchivoLocal = "C:/Users/joaquin.navedo_netpa/Desktop/medidas.csv";
            
        //     return java.nio.file.Files.readAllBytes(java.nio.file.Paths.get(rutaArchivoLocal));
        // } catch (Exception ex) {
        //     // Si llega aquí, es porque el archivo no existe o está mal escrito
        //     throw new RuntimeException("Error en simulación: No se pudo leer el archivo en: " + 
        //                                "C:/Users/joaquin.navedo_netpa/Desktop/medidas.csv. " +
        //                                "Asegúrate de que el archivo exista en el escritorio.", ex);
        // }
    }
}