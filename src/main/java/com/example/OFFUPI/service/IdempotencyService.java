package com.example.OFFUPI.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
@Service
public class IdempotencyService {

        private final StringRedisTemplate redisTemplate;

        @Value("${upi.mesh.idempotency-ttl-seconds}")
        private long ttlSeconds;

        public IdempotencyService(StringRedisTemplate redisTemplate) {
            this.redisTemplate = redisTemplate;
        }

        public boolean claim(String packetHash) {

            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(
                            packetHash,
                            "claimed",
                            Duration.ofSeconds(ttlSeconds)
                    );

            return Boolean.TRUE.equals(success);
        }

        public void clear(String packetHash) {
            redisTemplate.delete(packetHash);
        }
    }