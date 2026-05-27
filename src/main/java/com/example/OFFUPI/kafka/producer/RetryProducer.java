package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class RetryProducer {

    @Autowired
    private KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    // Send to Retry Topic
    public void sendToRetry(
            PaymentEvent event
    ) {

        kafkaTemplate.send(
                "payment-retry",
                event.getPacketHash(),
                event
        );

        System.out.println(
                "Sent to retry topic: "
                        + event.getPacketHash()
        );
    }

    // Send to Dead Letter Queue
    public void sendToDeadLetter(
            PaymentEvent event
    ) {

        kafkaTemplate.send(
                "payment-dead-letter",
                event.getPacketHash(),
                event
        );

        System.out.println(
                "Sent to DLQ: "
                        + event.getPacketHash()
        );
    }
}