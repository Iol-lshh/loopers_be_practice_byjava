package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductReader {
    Optional<ProductInfo.ProductWithSignal> findWithSignal(Long id);

    List<ProductInfo.ProductWithSignal> findWithSignals(ProductStatement criteria, Pageable pageable);

    List<ProductInfo.ProductWithSignal> findWithSignals(List<Long> ids);
}
