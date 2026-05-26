package com.example.OFFUPI.kafka.consumer;
import com.example.OFFUPI.event.PaymentEvent;
import com.example.OFFUPI.service.AsyncSettlementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventConsumer.class);

    @Autowired
    private AsyncSettlementService asyncSettlementService;

    @KafkaListener(
            topics = "payment-ingestion",
            groupId = "offupi-group"
    )
    public void consume(PaymentEvent event) {

        log.info(
                "Received payment event from Kafka: {}",
                event.getPacketHash()
        );

        asyncSettlementService.process(event);
    }
}