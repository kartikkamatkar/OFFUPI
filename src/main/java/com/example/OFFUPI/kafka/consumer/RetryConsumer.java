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
public class RetryConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(
                    RetryConsumer.class
            );

    @Autowired
    private AsyncSettlementService asyncSettlementService;

    @Autowired
    private RetryProducer retryProducer;

    @KafkaListener(
            topics = "payment-retry",
            groupId = "retry-group"
    )
    public void retry(
            PaymentEvent event
    ) {

        try {

            log.info(
                    "Retrying event: {}",
                    event.getPacketHash()
            );

            asyncSettlementService.process(event);

        } catch (Exception e) {

            log.error(
                    "Retry failed permanently: {}",
                    e.getMessage()
            );

            retryProducer.sendToDeadLetter(event);
        }
    }
}