// SUMMARY: This class represents a simulated device/node in the mesh network.
// Each device can hold packets, has internet or offline status, and can relay packets.
// VirtualDevice is NOT a Spring bean - it's a plain Java object used by MeshSimulatorService.

package com.example.OFFUPI.service;

import com.example.OFFUPI.entity.MeshPacket;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// This is NOT annotated with @Service or @Component - it's a regular Java class
// Instances are created manually (not by Spring)
public class VirtualDevice {

    // Unique identifier for this device (e.g., "phone-alice")
    private final String deviceId;

    // Whether this device has internet connection (can upload to backend)
    private final boolean hasInternet;

    // Map of packets held by this device (packetId -> MeshPacket)
    // ConcurrentHashMap allows safe access from multiple threads
    private final Map<String, MeshPacket> heldPackets =
            new ConcurrentHashMap<>();

    // Constructor - creates a new virtual device
    public VirtualDevice(
            String deviceId,
            boolean hasInternet
    ) {
        this.deviceId = deviceId;
        this.hasInternet = hasInternet;
    }

    // Get the device ID
    public String getDeviceId() {
        return deviceId;
    }

    // Check if device has internet
    public boolean hasInternet() {
        return hasInternet;
    }

    // Add a packet to this device's storage
    // putIfAbsent() only adds if packetId doesn't already exist (no duplicates)
    public void hold(MeshPacket packet) {
        heldPackets.putIfAbsent(
                packet.getPacketId(),
                packet
        );
    }

    // Get all packets held by this device
    public Collection<MeshPacket> getHeldPackets() {
        return heldPackets.values();
    }

    // Check if this device already holds a specific packet
    public boolean holds(String packetId) {
        return heldPackets.containsKey(packetId);
    }

    // Get how many packets this device is holding
    public int packetCount() {
        return heldPackets.size();
    }

    // Remove all packets from this device (reset)
    public void clear() {
        heldPackets.clear();
    }
}