// SUMMARY: This file configures Redis, which is an in-memory database (very fast).
// Redis stores data in RAM (computer memory) instead of hard drive → super fast for caching.
// StringRedisTemplate is a tool that helps your code easily save/get data from Redis.

package com.example.OFFUPI.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

// @Configuration - This class provides setup for Redis connection and operations
@Configuration
public class RedisConfig {

    // @Bean - Create and return a StringRedisTemplate that Spring will manage
    // StringRedisTemplate is like a remote control to talk to Redis server
    // It handles simple key-value pairs where both key and value are strings
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            // RedisConnectionFactory is automatically provided by Spring Boot
            // It knows how to connect to Redis (like connection details: host, port, password)
            // Spring auto-injects this because we have Redis dependency in pom.xml
            RedisConnectionFactory factory
    ) {

        // Create a new StringRedisTemplate using the connection factory
        // This template will be used throughout the app to:
        // - store data: template.opsForValue().set("user:123", "John")
        // - get data: template.opsForValue().get("user:123")
        // - delete data: template.delete("user:123")
        return new StringRedisTemplate(factory);
    }
}