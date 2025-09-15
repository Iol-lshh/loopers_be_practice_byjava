package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCacheRepository;
import com.loopers.domain.product.ProductInfo;
import com.loopers.domain.product.ProductStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
        String targets = redisTemplate.opsForValue().get(keyPattern);
        return deserializer.deserializeWithSignal(targets);
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

    @Override
    public Optional<ProductInfo.ProductWithSignal> findWithSignal(Long id) {
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(id);
        String target = redisTemplate.opsForValue().get(keyPattern);
        return deserializer.deserializeOneWithSignal(target);
    }

    @Override
    public ProductInfo.ProductWithSignal save(Long id, ProductInfo.ProductWithSignal info) {
        String keyPattern = ProductCacheKeyGenerator.withSignalFrom(id);
        String serialized = serializer.serializeOneWithSignal(info);
        redisTemplate.opsForValue().set(keyPattern, serialized);
        redisTemplate.expire(keyPattern, 60, TimeUnit.SECONDS);
        return info;
    }

    @Override
    public List<Long> findTodayRankingIds(Integer page, Integer size) {
        Set<String> set = redisTemplate.opsForZSet().reverseRange(
                "product:ranking:"+ LocalDate.now(),
                page * size,
                (page + 1) * size - 1
        );
        if (set == null || set.isEmpty()) {
            return Collections.emptyList();
        }
        
        return set.stream()
                .map(Long::valueOf)
                .collect(Collectors.toList());
    }
}
