package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductReader {
    Optional<ProductWithSignal> findWithSignal(Long id);

    List<ProductWithSignal> findWithSignals(ProductStatement criteria, Pageable pageable);

    List<ProductWithSignal> findWithSignals(List<Long> ids);
}
