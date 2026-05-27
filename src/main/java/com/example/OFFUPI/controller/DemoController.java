package com.example.OFFUPI.controller;

import com.example.OFFUPI.dto.SendMoneyRequest;
import com.example.OFFUPI.entity.MeshPacket;
import com.example.OFFUPI.service.BridgeIngestionService;
import com.example.OFFUPI.service.DemoService;
import com.example.OFFUPI.service.MeshSimulatorService;
import com.example.OFFUPI.service.MetricsService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin("*")
public class DemoController {

    @Autowired
    private MetricsService metricsService;

    @Autowired
    private DemoService demoService;

    @Autowired
    private MeshSimulatorService meshSimulatorService;

    @Autowired
    private BridgeIngestionService bridgeIngestionService;

    @PostMapping("/send")
    public String send(
            @RequestBody SendMoneyRequest request
    ) throws Exception {

        MeshPacket packet =
                demoService.createPacket(
                        request.getSenderVpa(),
                        request.getReceiverVpa(),
                        request.getAmount(),
                        request.getPin(),
                        5
                );

        meshSimulatorService.inject(
                "phone-alice",
                packet
        );

        metricsService.incrementMeshPackets();

        bridgeIngestionService.ingest(
                packet,
                "phone-bridge",
                3
        );

        return "Packet injected and sent to Kafka successfully";
    }
}