// SUMMARY: This is the main entry point for the entire Spring Boot application.
// It starts the embedded web server (Tomcat), initializes all beans, and begins listening for requests.
// @EnableKafka enables Kafka listener support for consuming messages from topics.

package com.example.OFFUPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

// @EnableKafka tells Spring: "Enable Kafka listener annotations like @KafkaListener"
// Without this, @KafkaListener annotations would be ignored
@EnableKafka

// @SpringBootApplication is a combination of three annotations:
//   1. @Configuration - This class has Spring configuration
//   2. @EnableAutoConfiguration - Spring automatically configures based on dependencies
//   3. @ComponentScan - Scan for components in this package and subpackages
@SpringBootApplication
public class OffupiApplication {

	// The main method - Java entry point
	// SpringApplication.run() starts the entire Spring Boot application
	public static void main(String[] args) {

		// SpringApplication.run() does MANY things:
		// 1. Starts embedded Tomcat server (default port 8080)
		// 2. Creates all beans (@Service, @Repository, @Controller, @Component)
		// 3. Sets up database connections
		// 4. Configures Kafka consumers and producers
		// 5. Starts listening for HTTP requests
		// 6. Starts Kafka listeners
		SpringApplication.run(OffupiApplication.class, args);
	}
}