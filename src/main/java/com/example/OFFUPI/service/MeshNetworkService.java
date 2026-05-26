package com.example.OFFUPI.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MeshNetworkService {

    private final Map<String, List<?>> devices =
            new HashMap<>();

    public MeshNetworkService() {

        devices.put("device-A", new ArrayList<>());
        devices.put("device-B", new ArrayList<>());
        devices.put("device-C", new ArrayList<>());
    }

    public Map<String, List<?>> getState() {
        return devices;
    }

    public void reset() {

        devices.values().forEach(List::clear);
    }
}