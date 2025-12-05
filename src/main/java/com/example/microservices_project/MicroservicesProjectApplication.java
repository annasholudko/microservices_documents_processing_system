package com.example.microservices_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class MicroservicesProjectApplication {

	public static void main(String[] args) {
		SpringApplication.run(MicroservicesProjectApplication.class, args);
	}

}
