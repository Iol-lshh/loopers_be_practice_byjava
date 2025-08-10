package com.loopers.config.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class RedisNodeInfo {
    private final String host;
    private final Integer port;
}
