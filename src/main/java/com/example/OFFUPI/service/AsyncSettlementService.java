package com.example.OFFUPI.service;

import com.example.OFFUPI.crypto.HybridCryptoService;
import com.example.OFFUPI.entity.PaymentInstruction;
import com.example.OFFUPI.event.PaymentEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AsyncSettlementService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    AsyncSettlementService.class
            );

    @Autowired
    private HybridCryptoService cryptoService;

    @Autowired
    private SettlementService settlementService;

    public void process(
            PaymentEvent event
    ) {

        try {

            log.info(
                    "Processing packet: {}",
                    event.getPacketHash()
            );

            PaymentInstruction instruction =
                    cryptoService.decrypt(
                            event.getPacket()
                                    .getCiphertext()
                    );

            settlementService.settle(
                    instruction,
                    event.getPacketHash(),
                    event.getBridgeNodeId(),
                    event.getHopCount()
            );

            log.info(
                    "Settlement completed: {}",
                    event.getPacketHash()
            );

        } catch (Exception e) {

            log.error(
                    "Settlement failed for packet {} : {}",
                    event.getPacketHash(),
                    e.getMessage(),
                    e
            );

            throw new RuntimeException(e);
        }
    }
}