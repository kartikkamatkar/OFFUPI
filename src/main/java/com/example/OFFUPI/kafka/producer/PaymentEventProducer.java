package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.core.KafkaTemplate;

import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publish(
            PaymentEvent event
    ) {

        System.out.println(
                "Publishing payment event: "
                        + event.getPacketHash()
        );

        kafkaTemplate.send(
                "payment-ingestion",
                event.getPacketHash(),
                event
        );

        System.out.println(
                "EVENT SENT TO KAFKA"
        );
    }
}