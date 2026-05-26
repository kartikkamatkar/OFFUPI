package com.example.OFFUPI.entity;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MeshPacket {

    @NotBlank(message = "Packet ID is required")
    private String packetId;

    @Min(value = 0, message = "TTL cannot be negative")
    private int ttl;

    @NotNull(message = "CreatedAt is required")
    private Long createdAt;

    @NotBlank(message = "Ciphertext is required")
    private String ciphertext;

    // ===========================
    // Constructors
    // ===========================

    public MeshPacket() {
    }

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
    // toString()
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