// SUMMARY: This entity represents a completed payment transaction stored in the database.
// After a payment is successfully processed, it's saved as a Transaction record.
// This provides an audit trail of all payments that went through the system.

package com.example.OFFUPI.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

// @Entity - This class IS stored in a database table
@Entity

// @Table defines the table name and database indexes
// indexes make database queries faster by pre-sorting columns
@Table(name = "transactions",
        // @Index creates a database index on packetHash column for faster lookups
        // unique = true means no two transactions can have the same packetHash
        // This prevents duplicate processing of the same packet
        indexes = { @Index(name = "idx_packet_hash", columnList = "packetHash", unique = true) })
public class Transaction {

    // @Id - Primary key of the table
    // @GeneratedValue - Database automatically generates this ID
    // IDENTITY strategy means auto-increment (1, 2, 3, 4...)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // SHA-256 hash of the encrypted packet (in hex format, 64 characters)
    // Used as idempotency key - prevents processing the same packet twice
    // unique = true ensures duplicate payments are rejected
    @Column(nullable = false, unique = true, length = 64)
    private String packetHash; // SHA-256 hex of the encrypted packet

    // Who sent the money (stored for audit)
    @Column(nullable = false)
    private String senderVpa;

    // Who received the money (stored for audit)
    @Column(nullable = false)
    private String receiverVpa;

    // How much money was sent (precision 19, scale 2 for accurate money storage)
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // When the sender originally created/signed the payment (from PaymentInstruction)
    // Instant = timestamp with timezone support (better than Date class)
    @Column(nullable = false)
    private Instant signedAt; // When the sender originally signed it (offline)

    // When our backend actually processed the transaction
    // This is set when the transaction is saved
    @Column(nullable = false)
    private Instant settledAt; // When the backend actually processed it

    // Which bridge node (phone that connects to backend) delivered this packet
    // Helps track which mesh node was used for delivery
    @Column(nullable = false)
    private String bridgeNodeId; // Which mesh node finally delivered it

    // How many devices this packet passed through (hops) before reaching a bridge
    // Higher hop count = more resilient but slower delivery
    @Column(nullable = false)
    private int hopCount; // How many devices it passed through

    // @Enumerated(EnumType.STRING) stores the enum name ("SETTLED" or "REJECTED") as string
    // If we used EnumType.ORDINAL, it would store 0, 1 (harder to understand in database)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    // Enum defines the possible states of a transaction
    // SETTLED = payment completed successfully
    // REJECTED = payment was denied (insufficient funds, invalid PIN, etc.)
    public enum Status { SETTLED, REJECTED }

    // Default constructor - required by JPA
    public Transaction() {}

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getPacketHash() { return packetHash; }
    public void setPacketHash(String packetHash) { this.packetHash = packetHash; }

    public String getSenderVpa() { return senderVpa; }
    public void setSenderVpa(String senderVpa) { this.senderVpa = senderVpa; }

    public String getReceiverVpa() { return receiverVpa; }
    public void setReceiverVpa(String receiverVpa) { this.receiverVpa = receiverVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public Instant getSignedAt() { return signedAt; }
    public void setSignedAt(Instant signedAt) { this.signedAt = signedAt; }

    public Instant getSettledAt() { return settledAt; }
    public void setSettledAt(Instant settledAt) { this.settledAt = settledAt; }

    public String getBridgeNodeId() { return bridgeNodeId; }
    public void setBridgeNodeId(String bridgeNodeId) { this.bridgeNodeId = bridgeNodeId; }

    public int getHopCount() { return hopCount; }
    public void setHopCount(int hopCount) { this.hopCount = hopCount; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}