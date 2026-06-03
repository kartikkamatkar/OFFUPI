// SUMMARY: This Kafka consumer listens to the "payment-retry" topic.
// It processes payments that failed in the main consumer but might succeed on retry.
// Examples of retryable failures: database connection issues, temporary network problems.

package com.example.OFFUPI.kafka.consumer;

import com.example.OFFUPI.event.PaymentEvent;
import com.example.OFFUPI.kafka.producer.RetryProducer;
import com.example.OFFUPI.service.AsyncSettlementService;
import com.example.OFFUPI.service.MetricsService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

// @Service - Spring manages this as a service bean
@Service
public class RetryConsumer {

    // Logger for recording retry processing events
    private static final Logger log =
            LoggerFactory.getLogger(RetryConsumer.class);

    // Service that tracks metrics (counts of successes, failures, retries)
    @Autowired
    private MetricsService metricsService;

    // Same settlement service used by main consumer
    @Autowired
    private AsyncSettlementService asyncSettlementService;

    // Producer that can send to retry topic (for more retries) or dead letter topic
    @Autowired
    private RetryProducer retryProducer;

    // @KafkaListener listening to "payment-retry" topic
    // Different groupId = "retry-group" so it doesn't conflict with main consumer
    @KafkaListener(
            topics = "payment-retry",
            groupId = "retry-group"
    )
    public void retry(PaymentEvent event) {

        // Try to process the payment again
        try {

            // Log that we're retrying this payment
            log.info(
                    "Retrying event: {}",
                    event.getPacketHash()
            );

            // Attempt to process the payment (same logic as main consumer)
            asyncSettlementService.process(event);

            // If successful, method ends here - payment is complete

        } catch (Exception e) {
            // If processing fails AGAIN on retry, we come here

            // Log permanent failure with full stack trace (e includes the error details)
            log.error(
                    "Retry failed permanently: {}",
                    e.getMessage(),
                    e  // e parameter includes full stack trace for debugging
            );

            // Increment Dead Letter Queue counter in metrics
            // This helps monitor how many payments are permanently failing
            metricsService.incrementDlq();

            // Send to Dead Letter Queue - this means "I give up, manual intervention needed"
            // The payment will NOT be retried automatically again
            retryProducer.sendToDeadLetter(event);
        }
    }
}