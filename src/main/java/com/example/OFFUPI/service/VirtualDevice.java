package com.example.OFFUPI.service;

import com.example.OFFUPI.entity.MeshPacket;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VirtualDevice {

    private final String deviceId;
    private final boolean hasInternet;
    private final Map<String, MeshPacket> heldPackets = new ConcurrentHashMap<>();

    public VirtualDevice(String deviceId, boolean hasInternet) {
        this.deviceId = deviceId;
        this.hasInternet = hasInternet;
    }

    public String getDeviceId() { return deviceId; }
    public boolean hasInternet() { return hasInternet; }

    public void hold(MeshPacket packet) {
        heldPackets.putIfAbsent(packet.getPacketId(), packet);
    }

    public Collection<MeshPacket> getHeldPackets() {
        return heldPackets.values();
    }

    public boolean holds(String packetId) {
        return heldPackets.containsKey(packetId);
    }

    public int packetCount() {
        return heldPackets.size();
    }

    public void clear() {
        heldPackets.clear();
    }
}
