// SUMMARY: This service simulates a mesh network of phones/devices.
// Packets "gossip" between devices until they reach a bridge node with internet.
// This simulates offline/off-grid payments where phones relay messages to each other.

package com.example.OFFUPI.service;

import com.example.OFFUPI.entity.MeshPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// @Service - This is a service that simulates mesh network behavior
@Service
public class MeshSimulatorService {

    // Logger for mesh simulation events
    private static final Logger log = LoggerFactory.getLogger(MeshSimulatorService.class);

    // Map of device ID to VirtualDevice object (thread-safe for concurrent access)
    // ConcurrentHashMap allows multiple threads to read/write safely
    private final Map<String, VirtualDevice> devices = new ConcurrentHashMap<>();

    // Constructor - automatically called when Spring creates this service
    public MeshSimulatorService() {
        // Create 5 simulated devices: 4 offline, 1 bridge with internet
        seedDefaultDevices();
    }

    // Creates the default set of mesh devices
    private void seedDefaultDevices() {
        // phone-alice - offline phone (no internet)
        devices.put("phone-alice",   new VirtualDevice("phone-alice",   false));

        // phone-stranger1 - offline phone (stranger's phone helps relay)
        devices.put("phone-stranger1", new VirtualDevice("phone-stranger1", false));

        // phone-stranger2 - offline phone
        devices.put("phone-stranger2", new VirtualDevice("phone-stranger2", false));

        // phone-stranger3 - offline phone
        devices.put("phone-stranger3", new VirtualDevice("phone-stranger3", false));

        // phone-bridge - has internet! (can upload to backend)
        devices.put("phone-bridge",  new VirtualDevice("phone-bridge",  true));
    }

    // Get all devices in the mesh
    public Collection<VirtualDevice> getDevices() {
        return devices.values();
    }

    // Get a specific device by its ID
    public VirtualDevice getDevice(String id) {
        return devices.get(id);
    }

    /**
     * Sender drops a packet into the mesh by handing it to their own device.
     * Called when someone wants to send a payment offline.
     */
    public void inject(String senderDeviceId, MeshPacket packet) {

        // Find the sender's device
        VirtualDevice sender = devices.get(senderDeviceId);

        // Throw error if device doesn't exist
        if (sender == null) throw new IllegalArgumentException("Unknown device: " + senderDeviceId);

        // Device holds/keeps the packet (like storing in phone memory)
        sender.hold(packet);

        // Log packet injection (showing first 8 chars of packet ID for readability)
        log.info("Packet {} injected at {} (TTL={})",
                packet.getPacketId().substring(0, 8), senderDeviceId, packet.getTtl());
    }

    /**
     * One round of gossip. Every device shares everything it has with every
     * other device. TTL is decremented per hop; packets at TTL 0 stay where
     * they are but are not forwarded further.
     *
     * Real BLE gossip would be pair-by-pair when devices come into range.
     * For the demo we let everyone gossip with everyone in one round, which
     * is equivalent to "fast-forward N rounds of pairwise gossip".
     */
    public GossipResult gossipOnce() {

        int transfers = 0;  // Count how many packet transfers happen
        List<VirtualDevice> deviceList = new ArrayList<>(devices.values());

        // Snapshot what each device holds at the start of this round, so
        // we don't gossip the same packet through 5 devices in 1 step.
        // This prevents "infinite loop" in one round
        Map<String, List<MeshPacket>> snapshot = new HashMap<>();
        for (VirtualDevice d : deviceList) {
            snapshot.put(d.getDeviceId(), new ArrayList<>(d.getHeldPackets()));
        }

        // For each source device in the mesh
        for (VirtualDevice src : deviceList) {
            // For each packet held by the source device (from snapshot)
            for (MeshPacket pkt : snapshot.get(src.getDeviceId())) {
                // Skip packet if TTL is 0 (can't forward anymore)
                if (pkt.getTtl() <= 0) continue;

                // For each destination device (try to send to everyone)
                for (VirtualDevice dst : deviceList) {
                    // Don't send to yourself
                    if (dst == src) continue;

                    // Skip if destination already has this packet
                    if (dst.holds(pkt.getPacketId())) continue;

                    // Create a COPY of the packet (don't modify original)
                    MeshPacket copy = new MeshPacket();
                    copy.setPacketId(pkt.getPacketId());
                    copy.setTtl(pkt.getTtl() - 1);  // DECREMENT TTL by 1
                    copy.setCreatedAt(pkt.getCreatedAt());
                    copy.setCiphertext(pkt.getCiphertext());

                    // Destination device holds the copy
                    dst.hold(copy);
                    transfers++;  // Count this transfer
                }
            }
        }

        // Log how many transfers happened this round
        log.info("Gossip round complete: {} packet transfers", transfers);

        // Return result with transfer count and current device packet counts
        return new GossipResult(transfers, snapshotMap());
    }

    // Create a snapshot of how many packets each device currently holds
    public Map<String, Integer> snapshotMap() {
        Map<String, Integer> m = new LinkedHashMap<>();
        for (VirtualDevice d : devices.values()) {
            m.put(d.getDeviceId(), d.packetCount());
        }
        return m;
    }

    /**
     * Returns all packets held by devices with internet — these are what would
     * be uploaded to the backend the moment they reach connectivity.
     */
    public List<BridgeUpload> collectBridgeUploads() {

        List<BridgeUpload> out = new ArrayList<>();

        // Check each device in the mesh
        for (VirtualDevice d : devices.values()) {
            // If device has internet (like a bridge node)
            if (!d.hasInternet()) continue;

            // Upload ALL packets held by this device
            for (MeshPacket pkt : d.getHeldPackets()) {
                out.add(new BridgeUpload(d.getDeviceId(), pkt));
            }
        }
        return out;
    }

    // Clear all packets from all devices (reset the mesh)
    public void resetMesh() {
        devices.values().forEach(VirtualDevice::clear);
    }

    // Record class for gossip result (immutable data holder)
    public record GossipResult(int transfers, Map<String, Integer> deviceCounts) {}

    // Record class for bridge upload (which bridge node, which packet)
    public record BridgeUpload(String bridgeNodeId, MeshPacket packet) {}
}