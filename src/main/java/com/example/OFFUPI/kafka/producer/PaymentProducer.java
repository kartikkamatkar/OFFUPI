package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.dto.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentProducer {

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publish(PaymentEvent event) {

        kafkaTemplate.send(
                "payment-ingestion",
                event.getPacketHash(),
                event
        );

        log.info("Payment event published to Kafka: {}",
                event.getPacketHash());
    }
}
