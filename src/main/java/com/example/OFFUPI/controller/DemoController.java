package com.example.OFFUPI.controller;

import com.example.OFFUPI.dto.SendMoneyRequest;
import com.example.OFFUPI.entity.MeshPacket;
import com.example.OFFUPI.service.DemoService;
import com.example.OFFUPI.service.MeshSimulatorService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo")
public class DemoController {

    @Autowired
    private DemoService demoService;

    @Autowired
    private MeshSimulatorService meshSimulatorService;

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

        return "packet injected into mesh";
    }
}