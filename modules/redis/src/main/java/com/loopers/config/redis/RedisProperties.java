package com.loopers.config.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(value = "datasource.redis")
public class RedisProperties {
    private final Integer database;
    private final RedisNodeInfo master;
    private final List<RedisNodeInfo> replicas;
}
