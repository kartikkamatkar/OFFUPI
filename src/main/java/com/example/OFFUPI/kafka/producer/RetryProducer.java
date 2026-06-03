// SUMMARY: This Kafka producer handles failed payments by sending them to retry or dead letter topics.
// It provides two methods:
// 1. sendToRetry() - for temporary failures that might succeed later
// 2. sendToDeadLetter() - for permanent failures that need manual review

package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// @Service - Spring manages this as a service bean
@Service
public class RetryProducer {

    // KafkaTemplate for sending messages to Kafka topics
    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // Send a failed event to the Retry Topic for another attempt
    // Use this for temporary failures (database down, network timeout, etc.)
    public void sendToRetry(
            PaymentEvent event
    ) {

        // Send to "payment-retry" topic (created in KafkaConfig)
        // Key is packetHash (for consistent partitioning)
        // Value is the event (to be retried)
        kafkaTemplate.send(
                "payment-retry",
                event.getPacketHash(),
                event
        );

        // Console output for debugging
        System.out.println(
                "Sent to retry topic: "
                        + event.getPacketHash()
        );
    }

    // Send a permanently failed event to Dead Letter Queue (DLQ)
    // Use this for permanent failures (invalid PIN, insufficient funds, etc.)
    // DLQ events require MANUAL investigation - no automatic retry
    public void sendToDeadLetter(
            PaymentEvent event
    ) {

        // Send to "payment-dead-letter" topic (final resting place for failed payments)
        kafkaTemplate.send(
                "payment-dead-letter",
                event.getPacketHash(),
                event
        );

        // Console output for debugging
        System.out.println(
                "Sent to DLQ: "
                        + event.getPacketHash()
        );
    }
}