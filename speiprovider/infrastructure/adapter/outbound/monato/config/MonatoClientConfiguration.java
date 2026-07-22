package com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.MonatoAuthInterceptor;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.client.MonatoHttpClient;

/**
 * Configura los clientes HTTP hacia Monato. Hay dos RestClient porque los endpoints de auth
 * (credential-tokens, credentials) no requieren Bearer token, mientras que el resto si.
 */
@Configuration
@EnableConfigurationProperties(MonatoProperties.class)
public class MonatoClientConfiguration {

    /**
     * Cliente para autenticacion: solo x-api-key, sin interceptor Bearer.
     * Lo usa {@link com.netpay.speiprovider.infrastructure.adapter.outbound.monato.auth.MonatoAuthService}.
     */
    @Bean
    @Qualifier("monatoAuthRestClient")
    public RestClient monatoAuthRestClient(MonatoProperties monatoProperties) {
        var builder = RestClient.builder()
                .baseUrl(monatoProperties.getBaseUrl())
                .defaultHeader("x-api-key", monatoProperties.getApiKey());
        return builder.build();
    }

    /**
     * Cliente declarativo de negocio hacia Monato. Construye internamente el RestClient que inyecta
     * Bearer + x-api-key via {@link MonatoAuthInterceptor}; lo usan adapters como cuentas, payouts, etc.
     */
    @Bean
    public MonatoHttpClient monatoHttpClient(MonatoAuthInterceptor authInterceptor, MonatoProperties monatoProperties) {
        var restClient = RestClient.builder()
                .baseUrl(monatoProperties.getBaseUrl())
                .requestInterceptor(authInterceptor)
                .build();
        var factory = HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(restClient))
                .build();
        return factory.createClient(MonatoHttpClient.class);
    }

}