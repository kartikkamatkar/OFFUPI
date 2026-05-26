package com.example.OFFUPI.dto;
import com.example.OFFUPI.entity.MeshPacket;

public class PaymentEvent {

    private String packetHash;
    private String bridgeNodeId;
    private int hopCount;
    private MeshPacket packet;

    public PaymentEvent() {
    }

    public PaymentEvent(
            String packetHash,
            String bridgeNodeId,
            int hopCount,
            MeshPacket packet
    ) {
        this.packetHash = packetHash;
        this.bridgeNodeId = bridgeNodeId;
        this.hopCount = hopCount;
        this.packet = packet;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private String packetHash;
        private String bridgeNodeId;
        private int hopCount;
        private MeshPacket packet;

        public Builder packetHash(String packetHash) {
            this.packetHash = packetHash;
            return this;
        }

        public Builder bridgeNodeId(String bridgeNodeId) {
            this.bridgeNodeId = bridgeNodeId;
            return this;
        }

        public Builder hopCount(int hopCount) {
            this.hopCount = hopCount;
            return this;
        }

        public Builder packet(MeshPacket packet) {
            this.packet = packet;
            return this;
        }

        public PaymentEvent build() {
            return new PaymentEvent(
                    packetHash,
                    bridgeNodeId,
                    hopCount,
                    packet
            );
        }
    }

    public String getPacketHash() {
        return packetHash;
    }

    public void setPacketHash(String packetHash) {
        this.packetHash = packetHash;
    }

    public String getBridgeNodeId() {
        return bridgeNodeId;
    }

    public void setBridgeNodeId(String bridgeNodeId) {
        this.bridgeNodeId = bridgeNodeId;
    }

    public int getHopCount() {
        return hopCount;
    }

    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    public MeshPacket getPacket() {
        return packet;
    }

    public void setPacket(MeshPacket packet) {
        this.packet = packet;
    }
}