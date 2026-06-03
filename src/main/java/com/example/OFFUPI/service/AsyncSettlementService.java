// SUMMARY: This service processes payments asynchronously from Kafka.
// It receives PaymentEvent objects, decrypts them, and calls SettlementService to complete the transfer.
// This is the main bridge between Kafka consumers and the actual payment logic.

package com.example.OFFUPI.service;

import com.example.OFFUPI.crypto.HybridCryptoService;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.event.PaymentEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// @Service tells Spring: "This class contains business logic for payment processing"
// Spring will create and manage one instance of this service
@Service
public class AsyncSettlementService {

    // Logger for recording processing events, successes, and failures
    private static final Logger log =
            LoggerFactory.getLogger(
                    AsyncSettlementService.class
            );

    // Service that handles encryption/decryption of payment instructions
    @Autowired
    private HybridCryptoService cryptoService;

    // Service that actually settles the payment (transfer money between accounts)
    @Autowired
    private SettlementService settlementService;

    // This method processes a payment event from Kafka
    // Called by PaymentEventConsumer or RetryConsumer
    public void process(
            PaymentEvent event
    ) {

        // Try-catch block - attempts to process, catches any errors
        try {

            // Log that we're starting to process this packet (for debugging)
            log.info(
                    "Processing packet: {}",
                    event.getPacketHash()
            );

            // STEP 1: Decrypt the ciphertext to get the payment instruction
            // event.getPacket().getCiphertext() gets the encrypted payment data
            // cryptoService.decrypt() uses server's private key to decrypt
            PaymentInstruction instruction =
                    cryptoService.decrypt(
                            event.getPacket()
                                    .getCiphertext()
                    );

            // STEP 2: Settle the payment (transfer money)
            // Pass decrypted instruction plus metadata to settlement service
            settlementService.settle(
                    instruction,                    // Payment details (sender, receiver, amount)
                    event.getPacketHash(),          // Unique hash for idempotency
                    event.getBridgeNodeId(),        // Which bridge node delivered it
                    event.getHopCount()             // How many hops through mesh
            );

            // Log successful completion
            log.info(
                    "Settlement completed: {}",
                    event.getPacketHash()
            );

        } catch (Exception e) {
            // If ANYTHING fails, we come here

            // Log the error with packet hash and error message
            log.error(
                    "Settlement failed for packet {} : {}",
                    event.getPacketHash(),
                    e.getMessage(),
                    e  // e includes full stack trace for debugging
            );

            // Re-throw the exception so Kafka consumer knows processing failed
            // This triggers retry logic in the consumer
            throw new RuntimeException(e);
        }
    }
}