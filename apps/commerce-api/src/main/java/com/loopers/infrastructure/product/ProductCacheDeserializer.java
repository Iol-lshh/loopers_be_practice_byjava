package com.loopers.infrastructure.product;

import java.util.List;

public class ProductCacheDeserializer {
    public static List<Long> deserializeIds(String targetIds) {
        if (targetIds == null || targetIds.isEmpty()) {
            return List.of();
        }
        String[] ids = targetIds.split(",");
        return List.of(ids).stream()
                .map(Long::valueOf)
                .toList();
    }
}
