package com.example.OFFUPI.event;
import com.example.OFFUPI.entity.MeshPacket;

public class PaymentEvent {

    private String packetHash;

    private String bridgeNodeId;

    private int hopCount;

    private MeshPacket packet;

    public PaymentEvent() {
    }

    public PaymentEvent(String packetHash,
                        String bridgeNodeId,
                        int hopCount,
                        MeshPacket packet) {

        this.packetHash = packetHash;
        this.bridgeNodeId = bridgeNodeId;
        this.hopCount = hopCount;
        this.packet = packet;
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