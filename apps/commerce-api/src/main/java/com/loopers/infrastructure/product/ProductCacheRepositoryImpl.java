package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductStatement;
import com.loopers.domain.product.ProductWithSignalEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ProductCacheRepositoryImpl implements ProductCacheRepository {
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public List<Long> findIds(ProductStatement statement, Pageable pageable) {
        if (pageable.getPageNumber() > 2) {
            return List.of();
        }
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(statement, pageable);
        String targetIds = redisTemplate.opsForValue().get(keyPattern);
        return ProductCacheDeserializer.deserializeIds(targetIds);
    }

    @Override
    public List<Long> save(ProductStatement criteria, Pageable pageable, List<ProductWithSignalEntity> productWithSignals) {
        if (pageable.getPageNumber() > 2) {
            return List.of();
        }
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(criteria, pageable);
        List<Long> ids = productWithSignals.stream()
                .map(ProductWithSignalEntity::getId)
                .toList();
        String serializedIds = ProductCacheSerializer.serializeIds(ids);
        redisTemplate.opsForValue().set(keyPattern, serializedIds);
        redisTemplate.expire(keyPattern, 600, TimeUnit.SECONDS);
        return ids;
    }
}
