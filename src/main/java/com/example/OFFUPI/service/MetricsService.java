// SUMMARY: This service tracks metrics and statistics for monitoring the payment system.
// It uses Micrometer to expose counters that can be viewed in dashboards (like Prometheus, Grafana).
// Metrics help monitor system health: success rate, failure rate, retry count, etc.

package com.example.OFFUPI.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Service;

// @Service - This service tracks application metrics
@Service
public class MetricsService {

    // Counter for successful payments
    private final Counter paymentSuccessCounter;

    // Counter for failed payments
    private final Counter paymentFailureCounter;

    // Counter for retry attempts
    private final Counter retryCounter;

    // Counter for dead letter queue entries (permanent failures)
    private final Counter dlqCounter;

    // Counter for mesh packets injected
    private final Counter meshPacketCounter;

    // Constructor - creates all the counters and registers them with Micrometer
    public MetricsService(
            MeterRegistry registry  // Micrometer's registry - collects all metrics
    ) {
        // Register a counter for settled transactions (old style)
        Counter.builder("settled_transactions_total")
                .register(registry);

        // Register a counter for offline payments (old style)
        Counter.builder("offline_payments_total")
                .register(registry);

        // Create success counter with description
        paymentSuccessCounter =
                Counter.builder(
                                "payment_success_total"  // Metric name for monitoring systems
                        )
                        .description(
                                "Total successful payments"  // Human-readable description
                        )
                        .register(registry);  // Register with Micrometer

        // Create failure counter
        paymentFailureCounter =
                Counter.builder(
                                "payment_failed_total"
                        )
                        .description(
                                "Total failed payments"
                        )
                        .register(registry);

        // Create retry counter
        retryCounter =
                Counter.builder(
                                "retry_total"
                        )
                        .description(
                                "Total retry attempts"
                        )
                        .register(registry);

        // Create DLQ (Dead Letter Queue) counter
        dlqCounter =
                Counter.builder(
                                "dlq_total"
                        )
                        .description(
                                "Total dead letter queue events"
                        )
                        .register(registry);

        // Create mesh packet counter
        meshPacketCounter =
                Counter.builder(
                                "mesh_packets_total"
                        )
                        .description(
                                "Total mesh packets injected"
                        )
                        .register(registry);
    }

    // Call this method when payment is successful
    public void incrementSuccess() {
        paymentSuccessCounter.increment();  // Increases counter by 1
    }

    // Call this method when payment fails
    public void incrementFailure() {
        paymentFailureCounter.increment();
    }

    // Call this method when a payment is sent to retry queue
    public void incrementRetry() {
        retryCounter.increment();
    }

    // Call this method when a payment goes to Dead Letter Queue
    public void incrementDlq() {
        dlqCounter.increment();
    }

    // Call this method when a packet enters the mesh
    public void incrementMeshPackets() {
        meshPacketCounter.increment();
    }
}