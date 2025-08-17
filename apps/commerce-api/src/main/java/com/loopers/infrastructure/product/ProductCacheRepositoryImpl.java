package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductStatement;
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
    private final ProductCacheSerializer.Serializer serializer;
    private final ProductCacheSerializer.Deserializer deserializer;

    @Override
    public List<ProductInfo.ProductWithSignal> findWithSignal(ProductStatement statement, Pageable pageable) {
        if (pageable.getPageNumber() > 2) {
            return List.of();
        }
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(statement, pageable);
        String targetIds = redisTemplate.opsForValue().get(keyPattern);
        return deserializer.deserializeWithSignal(targetIds);
    }

    @Override
    public List<ProductInfo.ProductWithSignal> save(ProductStatement criteria, Pageable pageable, List<ProductInfo.ProductWithSignal> productWithSignals) {
        if (pageable.getPageNumber() > 2) {
            return List.of();
        }
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(criteria, pageable);
        String serialized = serializer.serializeWithSignal(productWithSignals);
        redisTemplate.opsForValue().set(keyPattern, serialized);
        redisTemplate.expire(keyPattern, 60, TimeUnit.SECONDS);
        return productWithSignals;
    }
}
