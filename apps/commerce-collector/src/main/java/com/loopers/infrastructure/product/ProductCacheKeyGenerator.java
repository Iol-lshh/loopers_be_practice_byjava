package com.loopers.infrastructure.product;

public class ProductCacheKeyGenerator {
    public static String withSignalFrom(Long id) {
        return "product_with_signal-v1:" + id;
    }
}
