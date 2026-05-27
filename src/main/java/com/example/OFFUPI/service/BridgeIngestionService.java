package com.example.OFFUPI.service;

import com.example.OFFUPI.crypto.HybridCryptoService;
import com.example.OFFUPI.entity.MeshPacket;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.event.PaymentEvent;
import com.example.OFFUPI.kafka.producer.PaymentEventProducer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class BridgeIngestionService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    BridgeIngestionService.class
            );

    @Autowired
    private HybridCryptoService crypto;

    @Autowired
    private IdempotencyService idempotency;

    @Autowired
    private PaymentEventProducer producer;

    @Value("${upi.mesh.packet-max-age-seconds:86400}")
    private long maxAgeSeconds;

    public IngestResult ingest(
            MeshPacket packet,
            String bridgeNodeId,
            int hopCount
    ) {

        try {

            String packetHash =
                    crypto.hashCiphertext(
                            packet.getCiphertext()
                    );

            // =========================
            // IDEMPOTENCY CHECK
            // =========================

            if (!idempotency.claim(packetHash)) {

                log.info(
                        "DUPLICATE packet {} from bridge {} -> dropped",
                        packetHash.substring(0, 12) + "...",
                        bridgeNodeId
                );

                return IngestResult.duplicate(
                        packetHash
                );
            }

            // =========================
            // DECRYPT PACKET
            // =========================

            PaymentInstruction instruction;

            try {

                instruction =
                        crypto.decrypt(
                                packet.getCiphertext()
                        );

            } catch (Exception e) {

                log.warn(
                        "Decryption failed for packet {} : {}",
                        packetHash.substring(0, 12) + "...",
                        e.getMessage()
                );

                return IngestResult.invalid(
                        packetHash,
                        "decryption_failed"
                );
            }

            // =========================
            // REPLAY PROTECTION
            // =========================

            long ageSeconds =
                    (
                            Instant.now().toEpochMilli()
                                    - instruction.getSignedAt()
                    ) / 1000;

            if (ageSeconds > maxAgeSeconds) {

                log.warn(
                        "Packet {} too old ({}s)",
                        packetHash.substring(0, 12) + "...",
                        ageSeconds
                );

                return IngestResult.invalid(
                        packetHash,
                        "stale_packet"
                );
            }

            if (ageSeconds < -300) {

                return IngestResult.invalid(
                        packetHash,
                        "future_dated"
                );
            }

            // =========================
            // PUBLISH TO KAFKA
            // =========================

            PaymentEvent event =
                    new PaymentEvent(
                            packetHash,
                            bridgeNodeId,
                            hopCount,
                            packet
                    );

            producer.publish(event);

            log.info(
                    "Payment event published for {}",
                    packetHash.substring(0, 12) + "..."
            );

            // =========================
            // IMPORTANT:
            // NO DIRECT SETTLEMENT HERE
            // Kafka consumer handles settlement
            // =========================

            return new IngestResult(
                    "EVENT_PUBLISHED",
                    packetHash,
                    null,
                    null
            );

        } catch (Exception e) {

            log.error(
                    "Ingestion error : {}",
                    e.getMessage(),
                    e
            );

            return IngestResult.invalid(
                    "?",
                    "internal_error"
            );
        }
    }

    // =========================
    // RESULT RECORD
    // =========================

    public record IngestResult(
            String outcome,
            String packetHash,
            String reason,
            Long transactionId
    ) {

        public static IngestResult duplicate(
                String hash
        ) {

            return new IngestResult(
                    "DUPLICATE_DROPPED",
                    hash,
                    null,
                    null
            );
        }

        public static IngestResult invalid(
                String hash,
                String reason
        ) {

            return new IngestResult(
                    "INVALID",
                    hash,
                    reason,
                    null
            );
        }
    }
}