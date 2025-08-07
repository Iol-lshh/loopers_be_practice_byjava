package com.loopers.domain.product;

import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    ProductEntity save(ProductEntity product);

    Optional<ProductEntity> find(Long id);

    List<ProductEntity> findList(ProductStatement criteria, Pageable pageable);

    List<ProductEntity> findList(List<Long> ids);


    List<ProductEntity> findListWithLock(List<Long> ids);
}
