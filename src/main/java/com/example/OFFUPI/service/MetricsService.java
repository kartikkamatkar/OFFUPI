package com.example.OFFUPI.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;

import org.springframework.stereotype.Service;

@Service
public class MetricsService {

    private final Counter paymentSuccessCounter;

    private final Counter paymentFailureCounter;

    private final Counter retryCounter;

    private final Counter dlqCounter;

    private final Counter meshPacketCounter;

    public MetricsService(
            MeterRegistry registry
    ) {

        paymentSuccessCounter =
                Counter.builder(
                                "payment_success_total"
                        )
                        .description(
                                "Total successful payments"
                        )
                        .register(registry);

        paymentFailureCounter =
                Counter.builder(
                                "payment_failed_total"
                        )
                        .description(
                                "Total failed payments"
                        )
                        .register(registry);

        retryCounter =
                Counter.builder(
                                "retry_total"
                        )
                        .description(
                                "Total retries"
                        )
                        .register(registry);

        dlqCounter =
                Counter.builder(
                                "dlq_total"
                        )
                        .description(
                                "Total DLQ events"
                        )
                        .register(registry);

        meshPacketCounter =
                Counter.builder(
                                "mesh_packets_total"
                        )
                        .description(
                                "Total mesh packets"
                        )
                        .register(registry);
    }

    public void incrementSuccess() {
        paymentSuccessCounter.increment();
    }

    public void incrementFailure() {
        paymentFailureCounter.increment();
    }

    public void incrementRetry() {
        retryCounter.increment();
    }

    public void incrementDlq() {
        dlqCounter.increment();
    }

    public void incrementMeshPackets() {
        meshPacketCounter.increment();
    }
}