package com.example.OFFUPI.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentEvent
{

    private String packetHash;

    private String bridgeNodeId;

    private int hopCount;

    private MeshPacket packet;


}
