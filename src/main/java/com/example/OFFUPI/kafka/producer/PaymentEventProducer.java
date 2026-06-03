// SUMMARY: This Kafka producer sends payment events to the "payment-ingestion" topic.
// When the bridge receives a packet from the mesh network, this producer forwards it to Kafka.
// This decouples packet reception from payment processing (asynchronous processing).

package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

// @Service - Spring manages this as a service bean
@Service
public class PaymentEventProducer {

    // KafkaTemplate is the main tool for sending messages to Kafka
    // <String, PaymentEvent> means: key is String (packetHash), value is PaymentEvent
    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // This method publishes/sends a payment event to Kafka
    public void publish(
            PaymentEvent event
    ) {

        // Print to console that we're publishing (System.out for simple debugging)
        System.out.println(
                "Publishing payment event: "
                        + event.getPacketHash()
        );

        // Send the event to Kafka topic "payment-ingestion"
        // Parameters:
        //   1. "payment-ingestion" - topic name
        //   2. event.getPacketHash() - the key (used for partitioning)
        //   3. event - the actual message/payload
        kafkaTemplate.send(
                "payment-ingestion",
                event.getPacketHash(),
                event
        );

        // Print confirmation that message was sent
        System.out.println(
                "EVENT SENT TO KAFKA"
        );
    }
}