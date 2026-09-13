package com.karmperis.campapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CampAppBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampAppBackendApplication.class, args);
	}

}
