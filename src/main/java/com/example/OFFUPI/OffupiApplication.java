package com.example.OFFUPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class OffupiApplication {

	public static void main(String[] args) {
		SpringApplication.run(OffupiApplication.class, args);
	}

}
