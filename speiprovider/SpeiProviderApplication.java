package com.netpay.speiprovider;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ulisesbocchio.jasyptspringboot.annotation.EnableEncryptableProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootApplication
@EnableEncryptableProperties
public class SpeiProviderApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpeiProviderApplication.class, args);
		log.info("------------------------------------------------------");
		log.info("Microservicio spei-provider inicializado correctamente");
		log.info("------------------------------------------------------");
	}

}