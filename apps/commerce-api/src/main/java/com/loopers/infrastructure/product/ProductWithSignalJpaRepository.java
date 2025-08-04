package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductWithSignalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProductWithSignalJpaRepository extends JpaRepository<ProductWithSignalEntity, Long>,
        JpaSpecificationExecutor<ProductWithSignalEntity> {

} 