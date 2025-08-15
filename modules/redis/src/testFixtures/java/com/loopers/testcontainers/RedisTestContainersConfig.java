package com.loopers.testcontainers;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisTestContainersConfig {
    private static final RedisContainer redisContainer = new RedisContainer("redis:latest");
    static {
        redisContainer.start();
    }

    public RedisTestContainersConfig() {
        System.setProperty("datasource.redis.database", "0");
        System.setProperty("datasource.redis.master.host", redisContainer.getHost());
        System.setProperty("datasource.redis.host.port", redisContainer.getFirstMappedPort().toString());
        System.setProperty("datasource.redis.replicas[0].host", redisContainer.getHost());
        System.setProperty("datasource.redis.replicas[0].port", redisContainer.getFirstMappedPort().toString());
    }

}
