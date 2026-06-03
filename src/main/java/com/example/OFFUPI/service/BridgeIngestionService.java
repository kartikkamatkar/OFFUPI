// SUMMARY: This service handles incoming packets from bridge nodes (phones with internet).
// It performs critical checks: idempotency (no duplicates), decryption, replay protection (stale packets).
// Valid packets are published to Kafka for async processing. Invalid/duplicate packets are rejected.
// This is the ENTRY POINT for all payments coming from the mesh network into the backend.

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

// @Service - This class handles the ingestion/bridge logic for packets arriving from mesh
@Service
public class BridgeIngestionService {

    // Logger for recording ingestion events and errors
    private static final Logger log =
            LoggerFactory.getLogger(
                    BridgeIngestionService.class
            );

    // Service for encryption/decryption and hashing
    @Autowired
    private HybridCryptoService crypto;

    // Service that prevents duplicate payments using Redis
    @Autowired
    private IdempotencyService idempotency;

    // Producer that sends valid events to Kafka topic "payment-ingestion"
    @Autowired
    private PaymentEventProducer producer;

    // Maximum allowed age for a packet (in seconds)
    // Default: 86400 seconds = 24 hours
    // Packets older than this are rejected (replay protection)
    @Value("${upi.mesh.packet-max-age-seconds:86400}")
    private long maxAgeSeconds;

    // Main method called when a bridge node receives a packet from the mesh
    // Parameters:
    //   packet - The mesh packet containing encrypted payment data
    //   bridgeNodeId - Which bridge node received this packet
    //   hopCount - How many hops the packet traveled through the mesh
    public IngestResult ingest(
            MeshPacket packet,
            String bridgeNodeId,
            int hopCount
    ) {

        // Try-catch block - catches any unexpected errors during ingestion
        try {

            // STEP 1: Calculate the unique hash of the encrypted packet
            // This creates a digital fingerprint of the ciphertext
            String packetHash =
                    crypto.hashCiphertext(
                            packet.getCiphertext()
                    );

            // =========================
            // IDEMPOTENCY CHECK (Prevent Duplicates)
            // =========================

            // Try to claim this packet hash in Redis
            // claim() returns:
            //   - true if this is the FIRST time we've seen this packet
            //   - false if we've already processed this packet before
            if (!idempotency.claim(packetHash)) {

                // Log that we detected a duplicate packet
                // substring(0,12) shows first 12 chars of hash for readability
                log.info(
                        "DUPLICATE packet {} from bridge {} -> dropped",
                        packetHash.substring(0, 12) + "...",
                        bridgeNodeId
                );

                // Return duplicate result (packet is dropped, not processed)
                return IngestResult.duplicate(
                        packetHash
                );
            }

            // =========================
            // DECRYPT PACKET (Verify it's valid)
            // =========================

            PaymentInstruction instruction;

            // Try to decrypt the packet using server's private key
            try {

                instruction =
                        crypto.decrypt(
                                packet.getCiphertext()
                        );

            } catch (Exception e) {
                // If decryption fails, the packet is invalid/corrupted/tampered

                log.warn(
                        "Decryption failed for packet {} : {}",
                        packetHash.substring(0, 12) + "...",
                        e.getMessage()
                );

                // Return invalid result with reason "decryption_failed"
                return IngestResult.invalid(
                        packetHash,
                        "decryption_failed"
                );
            }

            // =========================
            // REPLAY PROTECTION (Prevent old/stale packets)
            // =========================

            // Calculate how old this packet is (in seconds)
            // Current time - packet creation time = age
            long ageSeconds =
                    (
                            Instant.now().toEpochMilli()    // Current time in milliseconds
                                    - instruction.getSignedAt()  // Packet creation time
                    ) / 1000;  // Convert milliseconds to seconds

            // Check if packet is TOO OLD (exceeds maximum allowed age)
            if (ageSeconds > maxAgeSeconds) {

                log.warn(
                        "Packet {} too old ({}s)",
                        packetHash.substring(0, 12) + "...",
                        ageSeconds
                );

                // Reject stale packet (prevents old captured packets from being replayed)
                return IngestResult.invalid(
                        packetHash,
                        "stale_packet"
                );
            }

            // Check if packet is from the FUTURE (clock mismatch or attack)
            // -300 seconds = 5 minutes grace period for clock differences
            if (ageSeconds < -300) {

                // Reject future-dated packets
                return IngestResult.invalid(
                        packetHash,
                        "future_dated"
                );
            }

            // =========================
            // PUBLISH TO KAFKA (Valid packet)
            // =========================

            // Create a PaymentEvent wrapping the packet and its metadata
            PaymentEvent event =
                    new PaymentEvent(
                            packetHash,        // Unique hash for idempotency
                            bridgeNodeId,      // Which bridge node delivered it
                            hopCount,          // How many hops through mesh
                            packet             // The actual mesh packet
                    );

            // Send event to Kafka topic "payment-ingestion"
            producer.publish(event);

            // Log successful publishing
            log.info(
                    "Payment event published for {}",
                    packetHash.substring(0, 12) + "..."
            );

            // =========================
            // IMPORTANT:
            // NO DIRECT SETTLEMENT HERE
            // Kafka consumer handles settlement asynchronously
            // =========================

            // Return success result
            return new IngestResult(
                    "EVENT_PUBLISHED",  // Outcome: successfully published
                    packetHash,         // The packet hash
                    null,              // No reason (success)
                    null               // No transaction ID yet (not settled)
            );

        } catch (Exception e) {
            // Catch any unexpected errors during the ingestion process

            log.error(
                    "Ingestion error : {}",
                    e.getMessage(),
                    e  // Full stack trace for debugging
            );

            // Return error result
            return IngestResult.invalid(
                    "?",  // Unknown packet hash (error occurred before hash calculation)
                    "internal_error"
            );
        }
    }

    // =========================
    // RESULT RECORD (Return type for ingest method)
    // =========================

    // Record (Java 14+) - immutable data holder for ingestion results
    // Similar to a class with final fields + constructor + getters
    public record IngestResult(
            String outcome,      // What happened: "EVENT_PUBLISHED", "DUPLICATE_DROPPED", "INVALID"
            String packetHash,   // The hash of the packet (for tracking)
            String reason,       // If invalid, why? ("decryption_failed", "stale_packet", etc.)
            Long transactionId   // If settled immediately, transaction ID (not used here)
    ) {

        // Static factory method for creating a DUPLICATE result
        public static IngestResult duplicate(
                String hash
        ) {

            return new IngestResult(
                    "DUPLICATE_DROPPED",  // Outcome: duplicate detected
                    hash,                  // Packet hash
                    null,                  // No reason needed
                    null                   // No transaction ID
            );
        }

        // Static factory method for creating an INVALID result
        public static IngestResult invalid(
                String hash,
                String reason
        ) {

            return new IngestResult(
                    "INVALID",     // Outcome: packet is invalid
                    hash,         // Packet hash
                    reason,       // Why invalid (e.g., "decryption_failed")
                    null          // No transaction ID
            );
        }
    }
}