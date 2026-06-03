// SUMMARY: This service provides demonstration/demo functionality for the payment system.
// It creates seed accounts (test users) and simulates packet creation on a phone.
// In a real app, packet creation would happen on Android devices, not the server.

package com.example.OFFUPI.service;

import com.example.OFFUPI.crypto.HybridCryptoService;
import com.example.OFFUPI.crypto.ServerKeyHolder;
import com.example.OFFUPI.entity.Account;
import com.example.OFFUPI.entity.MeshPacket;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.repository.AccountRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

// @Service marks this as a business logic service
@Service
public class DemoService {

    // Logger for recording demo activities
    private static final Logger log = LoggerFactory.getLogger(DemoService.class);

    // Repository for account database operations
    @Autowired
    private AccountRepository accounts;

    // Service for hybrid encryption (RSA + AES)
    @Autowired
    private HybridCryptoService crypto;

    // Holds the server's RSA key pair (public and private)
    @Autowired
    private ServerKeyHolder serverKey;

    // @PostConstruct means: "Run this method automatically after Spring creates this service"
    // This is perfect for initialization/seed data
    @PostConstruct
    public void seedAccounts() {

        // Check if database is empty (no accounts exist yet)
        if (accounts.count() == 0) {

            // Create 4 test accounts with different balances
            // kartik@demo - has 5000 rupees
            accounts.save(new Account("kartik@demo", "Kartik",   new BigDecimal("5000.00")));

            // pranay@demo - has 1000 rupees
            accounts.save(new Account("pranay@demo",   "pranay",     new BigDecimal("1000.00")));

            // tanmay@demo - has 2500 rupees
            accounts.save(new Account("tanmay@demo", "tanmay",   new BigDecimal("2500.00")));

            // devid@demo - has 500 rupees
            accounts.save(new Account("devid@demo",  "devid",    new BigDecimal("500.00")));

            // Log that seeding completed successfully
            log.info("Seeded 4 Static accounts");
        }
    }

    /**
     * Simulates the sender's phone:
     *   1. Build a PaymentInstruction with a fresh nonce + signedAt timestamp.
     *   2. Encrypt with the server's public key (hybrid RSA+AES).
     *   3. Wrap in a MeshPacket with TTL.
     *
     * In a real Android app, this exact code (minus the server-side reference)
     * would run on the phone. The phone would have already cached the server's
     * public key during a previous online session.
     */
    public MeshPacket createPacket(String senderVpa, String receiverVpa,
                                   BigDecimal amount, String pin, int ttl) throws Exception {

        // STEP 1: Create a payment instruction (unencrypted payment details)
        PaymentInstruction instruction = new PaymentInstruction(
                senderVpa,                           // Who is sending money
                receiverVpa,                         // Who is receiving money
                amount,                              // How much money
                sha256Hex(pin),                      // PIN hash (not plain PIN!)
                UUID.randomUUID().toString(),        // nonce — guarantees uniqueness (no duplicate payments)
                Instant.now().toEpochMilli()         // signedAt — for freshness check (timestamp)
        );

        // STEP 2: Encrypt the instruction with server's public key
        // Only the server (with private key) can decrypt this
        String ciphertext = crypto.encrypt(instruction, serverKey.getPublicKey());

        // STEP 3: Wrap encrypted data in a mesh packet (for network travel)
        MeshPacket packet = new MeshPacket();

        // Generate unique packet ID (for tracking through mesh)
        packet.setPacketId(UUID.randomUUID().toString());

        // Set TTL - how many hops this packet can survive
        packet.setTtl(ttl);

        // Set creation timestamp
        packet.setCreatedAt(Instant.now().toEpochMilli());

        // Store the encrypted payment data
        packet.setCiphertext(ciphertext);

        // Return the complete mesh packet
        return packet;
    }

    // Helper method: Convert a string to SHA-256 hash (hex format)
    // Example: "1234" → "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
    private String sha256Hex(String input) throws Exception {

        // MessageDigest creates cryptographic hashes
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Convert input string to bytes and compute hash
        byte[] hash = md.digest(input.getBytes());

        // Convert hash bytes to hexadecimal string (human-readable)
        StringBuilder hex = new StringBuilder();
        for (byte b : hash) {
            // %02x = format as 2-character hex, lowercase
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}