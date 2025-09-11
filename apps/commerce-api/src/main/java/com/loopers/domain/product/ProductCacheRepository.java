package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductCacheRepository {
    List<ProductInfo.ProductWithSignal> findWithSignal(ProductStatement criteria, Pageable pageable);

    List<ProductInfo.ProductWithSignal> save(ProductStatement criteria, Pageable pageable, List<ProductInfo.ProductWithSignal> productWithSignals);

    Optional<ProductInfo.ProductWithSignal> findWithSignal(Long id);

    ProductInfo.ProductWithSignal save(Long id, ProductInfo.ProductWithSignal info);
}
