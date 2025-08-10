package com.loopers.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisCleanUp {
    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    public void truncateAll() {
        redisConnectionFactory.getConnection().serverCommands().flushAll();
    }
}
