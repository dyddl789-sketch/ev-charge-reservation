package com.ev.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    private final StringRedisTemplate redisTemplate;

    public RedisTestController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis/test")
    public String redisTest() {

        // Redis 저장
        redisTemplate.opsForValue().set("test", "hello redis");

        // Redis 조회
        return redisTemplate.opsForValue().get("test");
    }
}
