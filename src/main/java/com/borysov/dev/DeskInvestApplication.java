package com.borysov.dev;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeskInvestApplication {

	public static void main(String[] args) {
		SpringApplication.run(DeskInvestApplication.class, args);
	}

}
