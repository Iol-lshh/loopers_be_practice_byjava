package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductReader {
    Optional<ProductWithSignalEntity> findWithSignal(Long id);

    List<ProductWithSignalEntity> findWithSignals(ProductStatement criteria, Pageable pageable);

    List<ProductWithSignalEntity> findWithSignals(List<Long> ids);
}
