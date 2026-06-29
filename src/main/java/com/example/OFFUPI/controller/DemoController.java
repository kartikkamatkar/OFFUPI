// SUMMARY: This controller demonstrates the payment flow through the mesh network.
// It creates a payment packet, sends it through mesh simulation, and processes via Kafka.
// URL pattern: /api/demo - main endpoint for demo operations.

package com.example.OFFUPI.controller;

import com.example.OFFUPI.dto.SendMoneyRequest;
import com.example.OFFUPI.entity.MeshPacket;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.crypto.HybridCryptoService;
import com.example.OFFUPI.service.BridgeIngestionService;
import com.example.OFFUPI.service.DemoService;
import com.example.OFFUPI.service.MeshSimulatorService;
import com.example.OFFUPI.service.MetricsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

// @RestController - This class handles API requests and returns JSON responses
@RestController

// All endpoints start with /api/demo (example: http://localhost:8080/api/demo/send)
@RequestMapping("/api/demo")

// @CrossOrigin("*") allows ANY frontend website to call these APIs
// "*" means "allow all origins" - useful for testing, but restrict in production
@CrossOrigin("*")
public class DemoController {

    // Auto-inject the service that tracks metrics (counters, statistics)
    @Autowired
    private MetricsService metricsService;

    // Auto-inject the service that contains demo business logic
    @Autowired
    private DemoService demoService;

    // Auto-inject the service that simulates the mesh network (like a mesh of phones)
    @Autowired
    private MeshSimulatorService meshSimulatorService;

    // Auto-inject the service that sends packets to Kafka for processing
    @Autowired
    private BridgeIngestionService bridgeIngestionService;

    // Auto-inject the crypto service for testing/decryption demonstrations
    @Autowired
    private HybridCryptoService hybridCryptoService;

    // This method handles POST requests to /api/demo/send
    // Purpose: Create a payment and process it through the entire system
    @PostMapping("/send")
    public String send(
            // @RequestBody tells Spring: "Take the JSON from HTTP request body and convert it to SendMoneyRequest object"
            // Example JSON: {"senderVpa": "alice@bank", "receiverVpa": "bob@bank", "amount": 100, "pin": "1234"}
            @RequestBody SendMoneyRequest request,
            // If true, settles the transaction immediately. Otherwise, it sits in the mesh network.
            @RequestParam(value = "immediate", defaultValue = "false") boolean immediate
    ) throws Exception {

        // STEP 1: Create a mesh packet (like a digital envelope containing payment details)
        // demoService.createPacket() converts the request into a MeshPacket object
        // Parameters: sender, receiver, amount, PIN, and maxHops (5 = can travel through 5 phones)
        MeshPacket packet =
                demoService.createPacket(
                        request.getSenderVpa(),    // Sender's Virtual Payment Address (like email for money)
                        request.getReceiverVpa(),  // Receiver's Virtual Payment Address
                        request.getAmount(),       // Money amount to send
                        request.getPin(),          // PIN for authentication
                        5                         // Maximum hops (network jumps) allowed
                );

        // STEP 2: Inject the packet into the simulated mesh network
        // "phone-alice" is the ID of the starting node (Alice's phone)
        // The packet will propagate through the mesh network
        meshSimulatorService.inject(
                "phone-alice",
                packet
        );

        // STEP 3: Increment the counter for monitoring
        // This helps track how many packets were created (for dashboards)
        metricsService.incrementMeshPackets();

        if (immediate) {
            // STEP 4: Send the packet to Kafka via the bridge service immediately
            // "phone-bridge" is a special node that connects mesh network to backend
            // 3 is the retry count (try 3 times if Kafka is busy)
            bridgeIngestionService.ingest(
                    packet,
                    "phone-bridge",
                    3
            );
            return "Packet injected and settled immediately";
        }

        // Return success message to the API caller indicating mesh placement
        return "Packet injected into mesh successfully";
    }

    // This method handles POST requests to /api/demo/decrypt
    // Purpose: Decrypt any packet's ciphertext using the server's private key for demonstration purposes
    @PostMapping("/decrypt")
    public PaymentInstruction decrypt(@RequestBody String ciphertext) throws Exception {
        // Strip outer JSON quotes if sent as double-quoted string
        if (ciphertext != null && ciphertext.startsWith("\"") && ciphertext.endsWith("\"")) {
            ciphertext = ciphertext.substring(1, ciphertext.length() - 1);
        }
        if (ciphertext == null || ciphertext.trim().isEmpty()) {
            throw new IllegalArgumentException("Ciphertext cannot be empty");
        }
        return hybridCryptoService.decrypt(ciphertext.trim());
    }
}