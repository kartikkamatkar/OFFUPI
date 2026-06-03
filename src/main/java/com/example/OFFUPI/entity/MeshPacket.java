// SUMMARY: This entity represents a packet traveling through the mesh network.
// A packet contains encrypted payment data and metadata (TTL, timestamp, etc.).
// This is NOT a database entity - it's just a regular Java object for data transfer.
// Packets hop from phone to phone until they reach a bridge node connected to the backend.

package com.example.OFFUPI.entity;

// Validation annotations - ensure data is valid before processing
import jakarta.validation.constraints.Min;      // Ensures number is not negative
import jakarta.validation.constraints.NotBlank; // Ensures string is not empty or null
import jakarta.validation.constraints.NotNull;  // Ensures value is not null

// No @Entity annotation here! This class is NOT stored in a database.
// It's just a plain Java object (POJO) used within the application.
public class MeshPacket {

    // @NotBlank ensures packetId cannot be null, empty, or just spaces
    // Every packet needs a unique ID to track it through the mesh
    @NotBlank(message = "Packet ID is required")
    private String packetId;

    // TTL = Time To Live - number of hops this packet can still make
    // Each time a packet moves to another device, TTL decreases by 1
    // When TTL reaches 0, packet is discarded (prevents infinite loops)
    // @Min(0) ensures TTL cannot be negative
    @Min(value = 0, message = "TTL cannot be negative")
    private int ttl;

    // @NotNull ensures createdAt timestamp is always present
    // Stores when the packet was created (Unix timestamp in milliseconds)
    @NotNull(message = "CreatedAt is required")
    private Long createdAt;

    // The encrypted payment data (Base64 encoded ciphertext)
    // This keeps the payment details secret as the packet travels through the mesh
    @NotBlank(message = "Ciphertext is required")
    private String ciphertext;

    // ===========================
    // Constructors
    // ===========================

    // No-argument constructor - required for frameworks like Jackson (JSON conversion)
    // Creates an empty packet that gets filled later via setters
    public MeshPacket() {
    }

    // Convenience constructor - creates a packet with all fields at once
    public MeshPacket(
            String packetId,
            int ttl,
            Long createdAt,
            String ciphertext
    ) {
        this.packetId = packetId;
        this.ttl = ttl;
        this.createdAt = createdAt;
        this.ciphertext = ciphertext;
    }

    // ===========================
    // Getters & Setters
    // ===========================

    public String getPacketId() {
        return packetId;
    }

    public void setPacketId(String packetId) {
        this.packetId = packetId;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public String getCiphertext() {
        return ciphertext;
    }

    public void setCiphertext(String ciphertext) {
        this.ciphertext = ciphertext;
    }

    // ===========================
    // toString() - creates a readable string representation
    // Used for logging and debugging
    // Note: Doesn't print ciphertext fully (would be too long)
    // ===========================

    @Override
    public String toString() {
        return "MeshPacket{" +
                "packetId='" + packetId + '\'' +
                ", ttl=" + ttl +
                ", createdAt=" + createdAt +
                ", ciphertext='" + ciphertext + '\'' +
                '}';
    }
}