package com.example.OFFUPI.controller;

import com.example.OFFUPI.service.BridgeIngestionService;
import com.example.OFFUPI.service.MeshSimulatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mesh")
public class MeshController {

    @Autowired
    private MeshSimulatorService meshSimulatorService;

    @Autowired
    private BridgeIngestionService bridgeIngestionService;

    @GetMapping("/state")
    public Object state() {

        return meshSimulatorService.getDevices();
    }

    @PostMapping("/gossip")
    public Object gossip() {

        return meshSimulatorService.gossipOnce();
    }

    @PostMapping("/flush")
    public String flush() {

        var uploads =
                meshSimulatorService.collectBridgeUploads();

        for (var upload : uploads) {

            bridgeIngestionService.ingest(
                    upload.packet(),
                    upload.bridgeNodeId(),
                    3
            );
        }

        return "bridge upload complete";
    }

    @PostMapping("/reset")
    public String reset() {

        meshSimulatorService.resetMesh();

        return "mesh reset";
    }
}