// SUMMARY: This Kafka consumer listens for payment events from the "payment-ingestion" topic.
// When a payment arrives from the mesh network, this consumer picks it up and processes it.
// This is the MAIN entry point for payment processing in the backend.

package com.example.OFFUPI.kafka.consumer;

import com.example.OFFUPI.event.PaymentEvent;
import com.example.OFFUPI.kafka.producer.RetryProducer;
import com.example.OFFUPI.service.AsyncSettlementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Service;

// @Service tells Spring: "This class contains business logic for consuming Kafka messages"
// Spring will create and manage one instance of this consumer
@Service
public class PaymentEventConsumer {

    // Logger for recording events, errors, and debugging information
    // SLF4J is a logging framework that writes to console/files
    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentEventConsumer.class
            );

    // Auto-inject the service that handles payment settlement asynchronously
    // This service contains the actual payment processing logic (deduct money, add money, etc.)
    @Autowired
    private AsyncSettlementService asyncSettlementService;

    // Auto-inject the producer that can send failed events to the retry topic
    // If processing fails, we send the event here to try again later
    @Autowired
    private RetryProducer retryProducer;

    // @KafkaListener tells Spring: "Listen to Kafka messages on this topic"
    // topics = "payment-ingestion" - listen to the topic where new payments arrive
    // groupId = "offupi-group" - consumer group ID (allows multiple consumers to share work)
    @KafkaListener(
            topics = "payment-ingestion",
            groupId = "offupi-group"
    )
    public void consume(PaymentEvent event) {

        // Log that we received an event - useful for debugging and monitoring
        log.info("KAFKA CONSUMER RECEIVED EVENT");

        // Log the packet hash (unique identifier) to track which payment we're processing
        // {} is a placeholder - SLF4J replaces it with the actual value
        log.info("Packet Hash: {}", event.getPacketHash());

        // Try-catch block - attempts to process, but catches any errors that occur
        try {

            // Log that we're starting to process this payment
            log.info(
                    "Processing event: {}",
                    event.getPacketHash()
            );

            // Call the settlement service to actually process the payment
            // This does the main work: decrypt packet, verify PIN, transfer money, save transaction
            asyncSettlementService.process(event);

        } catch (Exception e) {
            // If ANY error occurs during processing, we come here

            // Log the error message so we know what went wrong
            log.error(
                    "Main processing failed: {}",
                    e.getMessage()
            );

            // Send the failed event to the retry topic
            // The retry consumer will try to process it again later
            retryProducer.sendToRetry(event);
        }
    }
}