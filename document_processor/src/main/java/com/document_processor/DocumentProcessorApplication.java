package com.document_processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@SpringBootApplication
@EnableKafka
public class DocumentProcessorApplication {

	public static void main(String[] args) {
		SpringApplication.run(DocumentProcessorApplication.class, args);
	}

}
