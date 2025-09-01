package com.loopers.infrastructure.product;

import com.loopers.domain.catalog.CatalogCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CatalogCacheRepositoryImpl implements CatalogCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void evict(Long productId) {
        String key = ProductCacheKeyGenerator.withSignalFrom(productId);
        redisTemplate.delete(key);
    }
}
