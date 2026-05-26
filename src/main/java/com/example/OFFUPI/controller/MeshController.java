package com.example.OFFUPI.controller;

import com.example.OFFUPI.service.MeshNetworkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/mesh")
public class MeshController {

    @Autowired
    private MeshNetworkService meshService;

    @GetMapping("/state")
    public Map<String, List<?>> state() {

        return meshService.getState();
    }
    @PostMapping("/reset")
    public String reset() {

        meshService.reset();

        return "mesh reset";
    }
}