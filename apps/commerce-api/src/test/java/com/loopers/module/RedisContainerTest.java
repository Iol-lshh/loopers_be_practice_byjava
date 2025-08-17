package com.loopers.module;

import com.loopers.utils.RedisCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class RedisContainerTest {

    @Autowired
    RedisCleanUp redisCleanUp;

    @Autowired
    RedisTemplate<String, String> redisTemplate;

    @AfterEach
    void tearDown() {
        redisCleanUp.truncateAll();
    }

    @Test
    void contextLoads() {
        redisTemplate.opsForValue().set("testKey", "testValue");
        String result = redisTemplate.opsForValue().get("testKey");

        assertNotNull(result);
        assertEquals("testValue", result);

    }
}
