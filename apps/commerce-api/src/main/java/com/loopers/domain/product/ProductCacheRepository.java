package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductCacheRepository {
    List<Long> findIds(ProductStatement criteria, Pageable pageable);

    List<Long> save(ProductStatement criteria, Pageable pageable, List<ProductWithSignal> productWithSignals);
}
