package com.finedust.project.finedust;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinedustApplication {

	public static void main(String[] args) {
		SpringApplication.run(FinedustApplication.class, args);
	}

}
