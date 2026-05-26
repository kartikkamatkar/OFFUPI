package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.dto.PaymentEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentProducer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentProducer.class);

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // Constructor Injection
    public PaymentProducer(
            KafkaTemplate<String, PaymentEvent> kafkaTemplate
    ) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(PaymentEvent event) {

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