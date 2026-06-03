// SUMMARY: This service prevents duplicate payment processing using Redis.
// Idempotency means: processing the same payment twice has the same effect as once.
// Redis stores processed packet hashes with a time-to-live (TTL).

package com.example.OFFUPI.service;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.data.redis.core.RedisTemplate;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

// @Service marks this as a service for idempotency (duplicate prevention)
@Service
public class IdempotencyService {

    // RedisTemplate is a tool for talking to Redis (in-memory database)
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // @Value reads configuration from application.properties file
    // "${upi.mesh.idempotency-ttl-seconds:86400}" means:
    //   - Look for property "upi.mesh.idempotency-ttl-seconds"
    //   - If not found, use default 86400 seconds (24 hours)
    @Value("${upi.mesh.idempotency-ttl-seconds:86400}")
    private long ttlSeconds;

    // This method tries to "claim" a packet hash as being processed
    // Returns true if successful (first time), false if already processed (duplicate)
    public boolean claim(String packetHash) {

        // opsForValue() gives operations for simple key-value pairs
        // setIfAbsent() means: "Only set this key if it doesn't already exist"
        // This is atomic (thread-safe) - no race conditions!
        Boolean success =
                redisTemplate.opsForValue()
                        .setIfAbsent(
                                packetHash,           // Key: the packet hash
                                "processed",          // Value: just a marker
                                ttlSeconds,           // Time to live (expiry)
                                TimeUnit.SECONDS      // Unit for TTL
                        );

        // Return true if set was successful (first time), false if key already existed
        // Boolean.TRUE.equals(success) safely handles null (success could be null)
        return Boolean.TRUE.equals(success);
    }
}