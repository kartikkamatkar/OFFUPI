package com.example.OFFUPI.kafka.consumer;

import com.example.OFFUPI.event.PaymentEvent;
import com.example.OFFUPI.kafka.producer.RetryProducer;
import com.example.OFFUPI.service.AsyncSettlementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.kafka.annotation.KafkaListener;

import org.springframework.stereotype.Service;

@Service
public class PaymentEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PaymentEventConsumer.class
            );

    @Autowired
    private AsyncSettlementService asyncSettlementService;

    @Autowired
    private RetryProducer retryProducer;

    @KafkaListener(
            topics = "payment-ingestion",
            groupId = "offupi-group"
    )
    public void consume(PaymentEvent event) {

        log.info("KAFKA CONSUMER RECEIVED EVENT");
        log.info("Packet Hash: {}", event.getPacketHash());

        try {

            log.info(
                    "Processing event: {}",
                    event.getPacketHash()
            );

            asyncSettlementService.process(event);

        } catch (Exception e) {

            log.error(
                    "Main processing failed: {}",
                    e.getMessage()
            );

            retryProducer.sendToRetry(event);
        }
    }
}