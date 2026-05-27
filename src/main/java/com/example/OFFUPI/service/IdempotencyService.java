package com.example.OFFUPI.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class IdempotencyService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Value("${upi.mesh.idempotency-ttl-seconds:86400}")
    private long ttlSeconds;

    public boolean claim(String packetHash) {

        Boolean success =
                redisTemplate.opsForValue()
                        .setIfAbsent(
                                packetHash,
                                "processed",
                                ttlSeconds,
                                TimeUnit.SECONDS
                        );

        return Boolean.TRUE.equals(success);
    }
}