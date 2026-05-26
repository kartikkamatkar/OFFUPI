package com.example.OFFUPI.kafka.producer;

import com.example.OFFUPI.event.PaymentEvent;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    public void publish(PaymentEvent event) {

        System.out.println(
                "Publishing payment event: "
                        + event.getPacketHash()
        );
    }
}