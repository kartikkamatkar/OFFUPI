// SUMMARY: This class represents an event that is triggered when a payment arrives at the bridge.
// Events are used for asynchronous communication between different parts of the application.
// When a mesh packet reaches a bridge node, we wrap it in a PaymentEvent and publish it.
// Other parts of the app (like Kafka producers) listen for these events and process them.

package com.example.OFFUPI.event;

import com.example.OFFUPI.entity.MeshPacket;

// This is a Plain Old Java Object (POJO) - no special annotations
// It's a simple data carrier that holds information about an incoming payment
// Events are often used with Spring's ApplicationEventPublisher or messaging systems
public class PaymentEvent {

    // SHA-256 hash of the encrypted packet (in hex format)
    // Used as a unique identifier to prevent duplicate processing
    // This is the same as the packetHash stored in Transaction entity
    private String packetHash;

    // Which bridge node (phone that connects mesh to backend) delivered this packet
    // Example: "phone-bridge-001" or "gateway-node-5"
    // Helps track which part of the mesh network delivered the payment
    private String bridgeNodeId;

    // Number of hops (devices) this packet traveled through the mesh network
    // Example: hopCount = 3 means packet went: Phone A → Phone B → Phone C → Bridge
    // Higher hop count means the packet survived through more intermediate devices
    private int hopCount;

    // The actual mesh packet containing encrypted payment data
    // This is the original packet that traveled through the mesh network
    // Contains the ciphertext, TTL, packetId, and timestamp
    private MeshPacket packet;

    // Default constructor - required for frameworks like Jackson (JSON deserialization)
    // Also used by Spring when creating event objects via reflection
    public PaymentEvent() {
    }

    // Convenience constructor - creates a complete event with all fields
    // This is the preferred way to create PaymentEvent objects
    public PaymentEvent(
            String packetHash,
            String bridgeNodeId,
            int hopCount,
            MeshPacket packet
    ) {
        // 'this' refers to the current object's fields
        // Without 'this', Java would assign to the constructor parameters (shadowing)
        this.packetHash = packetHash;
        this.bridgeNodeId = bridgeNodeId;
        this.hopCount = hopCount;
        this.packet = packet;
    }

    // Getter for packetHash - allows other code to READ the hash value
    // Used by Kafka consumer to check for duplicate transactions
    public String getPacketHash() {
        return packetHash;
    }

    // Setter for packetHash - allows other code to WRITE/update the hash
    // Used when creating or modifying the event
    public void setPacketHash(String packetHash) {
        this.packetHash = packetHash;
    }

    // Getter for bridgeNodeId - reads which bridge node delivered the packet
    // Used for analytics and debugging (which mesh nodes are most active)
    public String getBridgeNodeId() {
        return bridgeNodeId;
    }

    // Setter for bridgeNodeId - sets the bridge node identifier
    public void setBridgeNodeId(String bridgeNodeId) {
        this.bridgeNodeId = bridgeNodeId;
    }

    // Getter for hopCount - reads how many hops the packet traveled
    // Used for network performance analysis
    public int getHopCount() {
        return hopCount;
    }

    // Setter for hopCount - sets the hop count
    public void setHopCount(int hopCount) {
        this.hopCount = hopCount;
    }

    // Getter for packet - returns the actual MeshPacket object
    // This contains the encrypted payment data that needs processing
    public MeshPacket getPacket() {
        return packet;
    }

    // Setter for packet - sets the mesh packet
    public void setPacket(MeshPacket packet) {
        this.packet = packet;
    }
}