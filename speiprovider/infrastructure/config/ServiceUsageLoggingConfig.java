package com.netpay.speiprovider.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netpay.speiprovider.infrastructure.adapter.outbound.monato.config.MonatoProperties;

/**
 * Registra interceptor + filter de logging de uso de servicios cuando
 * {@code spei.logging.service-usage.enabled=true}.
 */
@Configuration
@EnableConfigurationProperties({ ServiceUsageLoggingProperties.class, MonatoProperties.class })
public class ServiceUsageLoggingConfig implements WebMvcConfigurer {

	private final ServiceUsageLoggingProperties properties;
	private final ServiceUsagePayloadCachingFilter payloadFilter;
	private final ServiceUsageLoggingInterceptor interceptor;

	public ServiceUsageLoggingConfig(
			ServiceUsageLoggingProperties properties,
			MonatoProperties monatoProperties,
			ObjectMapper objectMapper) {
		this.properties = properties;
		this.payloadFilter = new ServiceUsagePayloadCachingFilter(properties, objectMapper);
		this.interceptor = new ServiceUsageLoggingInterceptor(properties, monatoProperties, payloadFilter);
	}

	@Bean
	public FilterRegistrationBean<ServiceUsagePayloadCachingFilter> serviceUsagePayloadCachingFilterRegistration() {
		FilterRegistrationBean<ServiceUsagePayloadCachingFilter> registration = new FilterRegistrationBean<>();
		registration.setFilter(payloadFilter);
		registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 20);
		registration.setName("serviceUsagePayloadCachingFilter");
		if (properties.getPaths() != null && !properties.getPaths().isEmpty()) {
			registration.addUrlPatterns(toUrlPatterns(properties.getPaths()));
		}
		registration.setEnabled(properties.isEnabled() && properties.isLogPayload());
		return registration;
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		if (!properties.isEnabled()) {
			return;
		}
		var registration = registry.addInterceptor(interceptor);
		if (properties.getPaths() != null && !properties.getPaths().isEmpty()) {
			registration.addPathPatterns(properties.getPaths().toArray(String[]::new));
		}
	}

	/**
	 * Url patterns de Filter usan prefijo con slash; se reutilizan los paths configurados.
	 */
	private String[] toUrlPatterns(java.util.List<String> paths) {
		return paths.stream()
				.map(path -> path.endsWith("/**") ? path : path)
				.toArray(String[]::new);
	}
}
