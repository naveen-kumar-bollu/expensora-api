package com.expensora.expensora_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExpensoraApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExpensoraApiApplication.class, args);
	}

}
