package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

@Service
public class RetryProducer {

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void sendToRetry(
            PaymentEvent event
    ) {

        kafkaTemplate.send(
                "payment-retry",
                event
        );
    }

    public void sendToDeadLetter(
            PaymentEvent event
    ) {

        kafkaTemplate.send(
                "payment-dead-letter",
                event
        );
    }
}