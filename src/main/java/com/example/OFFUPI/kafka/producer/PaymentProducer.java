package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public PaymentProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(PaymentEvent event) {

        log.info("=================================");
        log.info("ENTERED PRODUCER METHOD");
        log.info("Packet Hash: {}", event.getPacketHash());
        log.info("Sending to topic: payment-ingestion");
        log.info("=================================");

        kafkaTemplate.send(
                "payment-ingestion",
                event.getPacketHash(),
                event
        );

        log.info(
                "Payment event published to Kafka: {}",
                event.getPacketHash()
        );
    }
}