// SUMMARY: This controller manages the mesh network simulation.
// It allows inspecting, controlling, and resetting the mesh network of devices.
// URL pattern: /api/mesh - all mesh-related operations.

package com.example.OFFUPI.controller;

import com.example.OFFUPI.service.BridgeIngestionService;
import com.example.OFFUPI.service.MeshSimulatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// @RestController - Returns JSON data for mesh API calls
@RestController

// All endpoints start with /api/mesh (example: /api/mesh/state)
@RequestMapping("/api/mesh")
public class MeshController {

    // Service that simulates the mesh network (all connected devices)
    @Autowired
    private MeshSimulatorService meshSimulatorService;

    // Service that sends mesh packets to Kafka for processing
    @Autowired
    private BridgeIngestionService bridgeIngestionService;

    // This method handles GET requests to /api/mesh/state
    // Purpose: Get the current state of all devices in the mesh network
    @GetMapping("/state")
    public Object state() {

        // Get all devices and their connection status from the mesh simulator
        // Return as JSON so frontend can display network topology
        return meshSimulatorService.getDevices();
    }

    // This method handles POST requests to /api/mesh/gossip
    // Purpose: Trigger one round of "gossip protocol" - nodes share information with neighbors
    // Gossip is like a rumor spreading - each node tells its neighbors what it knows
    @PostMapping("/gossip")
    public Object gossip() {

        // Run one round of gossip protocol across the mesh network
        // Returns which packets were exchanged
        return meshSimulatorService.gossipOnce();
    }

    // This method handles POST requests to /api/mesh/flush
    // Purpose: Take all packets that reached bridge nodes and send them to Kafka
    // Bridge nodes are special nodes that connect mesh network to central backend
    @PostMapping("/flush")
    public String flush() {

        // Collect all packets that are waiting at bridge nodes (ready to upload)
        // var is a shortcut - Java automatically figures out the type
        var uploads =
                meshSimulatorService.collectBridgeUploads();

        // Loop through each upload packet
        for (var upload : uploads) {

            // Send each packet to Kafka for processing
            // upload.packet() - the actual payment data
            // upload.bridgeNodeId() - which bridge node is uploading
            // 3 - number of retry attempts if Kafka is unavailable
            bridgeIngestionService.ingest(
                    upload.packet(),
                    upload.bridgeNodeId(),
                    3
            );
        }

        // Return success message
        return "bridge upload complete";
    }

    // This method handles POST requests to /api/mesh/reset
    // Purpose: Clear the mesh network and start fresh (remove all packets, reset state)
    @PostMapping("/reset")
    public String reset() {

        // Tell the mesh simulator to clear all devices and pending packets
        meshSimulatorService.resetMesh();

        // Return confirmation message
        return "mesh reset";
    }
}