// SUMMARY: This entity represents the actual payment details AFTER decryption.
// When a MeshPacket arrives, it's decrypted to reveal a PaymentInstruction.
// Contains the core payment data: who sends, who receives, how much, and proof.

package com.example.OFFUPI.entity;

import java.math.BigDecimal;

// No @Entity - this is NOT stored directly in a database table
// It's a DTO-like object used inside the application after decryption
public class PaymentInstruction {

    // Who is sending the money (Virtual Payment Address)
    private String senderVpa;

    // Who is receiving the money (Virtual Payment Address)
    private String receiverVpa;

    // How much money to send (using BigDecimal for accuracy)
    private BigDecimal amount;

    // PIN hash - NOT the actual PIN, but a hashed version
    // Hash = one-way encryption that cannot be reversed
    // The backend can hash the entered PIN and compare hashes without storing real PIN
    private String pinHash;

    // Nonce = "number used once" - a unique identifier for this payment intent
    // Prevents replay attacks (same payment being submitted twice)
    // Usually a UUID (Universally Unique Identifier)
    private String nonce;     // UUID, unique per payment intent

    // Timestamp (in milliseconds since 1970-01-01) when sender created/signed this instruction
    // Helps detect old/stale payments and prevents timing attacks
    private Long signedAt;    // epoch millis, when sender signed

    // Default constructor - creates empty instruction (for Jackson deserialization)
    public PaymentInstruction() {}

    // Convenience constructor - creates a complete instruction
    public PaymentInstruction(String senderVpa, String receiverVpa, BigDecimal amount,
                              String pinHash, String nonce, Long signedAt) {
        this.senderVpa = senderVpa;
        this.receiverVpa = receiverVpa;
        this.amount = amount;
        this.pinHash = pinHash;
        this.nonce = nonce;
        this.signedAt = signedAt;
    }

    // Getters and setters - allow controlled access to private fields
    public String getSenderVpa() { return senderVpa; }
    public void setSenderVpa(String senderVpa) { this.senderVpa = senderVpa; }

    public String getReceiverVpa() { return receiverVpa; }
    public void setReceiverVpa(String receiverVpa) { this.receiverVpa = receiverVpa; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getPinHash() { return pinHash; }
    public void setPinHash(String pinHash) { this.pinHash = pinHash; }

    public String getNonce() { return nonce; }
    public void setNonce(String nonce) { this.nonce = nonce; }

    public Long getSignedAt() { return signedAt; }
    public void setSignedAt(Long signedAt) { this.signedAt = signedAt; }
}