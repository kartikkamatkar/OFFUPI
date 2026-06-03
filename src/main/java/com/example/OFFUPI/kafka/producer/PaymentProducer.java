// SUMMARY: This Kafka producer sends payment events to the "payment-ingestion" Kafka topic.
// When the bridge receives a packet from the mesh network, it creates a PaymentEvent
// and this producer forwards it to Kafka for asynchronous processing.
// Using Kafka decouples the network bridge (fast) from payment processing (slower).

package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

// @Service tells Spring: "This class contains business logic and should be managed by Spring"
// Spring will create a single instance (singleton) of this producer and inject its dependencies
@Service
public class PaymentProducer {

    // Logger for recording messages to console/log file
    // This helps with debugging and monitoring the producer's activity
    // "private static final" means: one logger for the whole class, cannot be changed
    private static final Logger log =
            LoggerFactory.getLogger(PaymentProducer.class);

    // KafkaTemplate is Spring's tool for sending messages to Kafka topics
    // <String, PaymentEvent> means:
    //   - Key type: String (we use packetHash as the key)
    //   - Value type: PaymentEvent (the actual message/payload)
    // KafkaTemplate handles serialization, partitioning, and network communication
    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // Constructor - called by Spring to create this service
    // This is Constructor Injection (recommended over @Autowired field injection)
    // Spring automatically provides the KafkaTemplate bean when creating PaymentProducer
    public PaymentProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate
    ) {
        // 'this.kafkaTemplate' refers to the class field
        // 'kafkaTemplate' (without 'this') is the constructor parameter
        // This assigns the injected KafkaTemplate to our class field
        this.kafkaTemplate = kafkaTemplate;
    }

    // This method publishes/sends a PaymentEvent to Kafka
    // Called by BridgeIngestionService when a packet arrives from the mesh network
    public void publish(PaymentEvent event) {

        // Log a separator line - makes logs easier to read
        log.info("=================================");

        // Log that we entered the producer method (for debugging flow)
        log.info("ENTERED PRODUCER METHOD");

        // Log the unique packet hash - helps track individual payments
        // {} is a placeholder - SLF4J replaces it with event.getPacketHash()
        log.info("Packet Hash: {}", event.getPacketHash());

        // Log which topic we're sending to (for debugging)
        log.info("Sending to topic: payment-ingestion");
        log.info("=================================");

        // Send the event to Kafka topic "payment-ingestion"
        // Parameters:
        //   1. "payment-ingestion" - the topic name (created in KafkaConfig)
        //   2. event.getPacketHash() - the message KEY (used for partitioning)
        //      Same packetHash always goes to same partition (guarantees order)
        //   3. event - the message VALUE (the actual payment data)
        kafkaTemplate.send(
                "payment-ingestion",
                event.getPacketHash(),
                event
        );

        // Log confirmation that the message was sent successfully
        // This doesn't mean Kafka received it yet, just that it was handed off
        log.info(
                "Payment event published to Kafka: {}",
                event.getPacketHash()
        );

        // Method ends here - sending is asynchronous (doesn't wait for confirmation)
        // The actual delivery to Kafka happens in the background
    }
}