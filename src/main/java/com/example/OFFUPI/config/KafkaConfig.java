// SUMMARY: This file creates Kafka topics (like chat rooms or channels) for payment processing.
// Kafka is a message queue system - it stores and delivers messages between different parts of your app.
// Topics are like named channels where messages are organized (similar to folders or categories).

package com.example.OFFUPI.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

// @Configuration tells Spring: "This class contains setup/configuration for the app"
@Configuration
public class KafkaConfig {

    // @Bean - This method creates a Kafka topic object that Spring will manage
    // payment-ingestion topic: Where new payments first arrive before processing
    @Bean
    public NewTopic paymentIngestionTopic() {
        // TopicBuilder is a helper class that makes it easy to create topic configurations
        return TopicBuilder
                // .name() sets the unique name of this topic (like a database table name)
                .name("payment-ingestion")
                // .partitions(3) splits the topic into 3 parts for parallel processing
                // More partitions = more speed, because 3 consumers can work simultaneously
                .partitions(3)
                // .replicas(1) creates 1 backup copy of the data
                // Replicas provide fault tolerance - if one server fails, another has the data
                .replicas(1)
                // .build() creates the actual NewTopic object from our settings
                .build();
    }

    // payment-settled topic: Where successfully processed payments go
    @Bean
    public NewTopic paymentSettledTopic() {
        return TopicBuilder
                .name("payment-settled")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // payment-invalid topic: Where failed/incorrect payments go for review
    @Bean
    public NewTopic paymentInvalidTopic() {
        return TopicBuilder
                .name("payment-invalid")
                .partitions(3)
                .replicas(1)
                .build();
    }

    // retry topic: Where payments that temporarily failed go to be tried again
    // Example: If a database is down, payment goes here and we retry later
    @Bean
    public NewTopic retryTopic() {
        return TopicBuilder.name(
                        "payment-retry"
                )
                .partitions(3)
                .replicas(1)
                .build();
    }

    // dead-letter topic: Where payments that permanently failed go (after all retries exhausted)
    // "Dead letter" means a message that could never be delivered - needs manual inspection
    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(
                        "payment-dead-letter"
                )
                .partitions(3)
                .replicas(1)
                .build();
    }
}