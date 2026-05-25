package com.example.OFFUPI.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

        @Bean
        public NewTopic paymentIngestionTopic() {
            return TopicBuilder
                    .name("payment-ingestion")
                    .partitions(3)
                    .replicas(1)
                    .build();
        }

        @Bean
        public NewTopic paymentSettledTopic() {
            return TopicBuilder
                    .name("payment-settled")
                    .partitions(3)
                    .replicas(1)
                    .build();
        }

        @Bean
        public NewTopic paymentInvalidTopic() {
            return TopicBuilder
                    .name("payment-invalid")
                    .partitions(3)
                    .replicas(1)
                    .build();
        }
    }
