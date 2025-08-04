package com.loopers.domain.product;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ProductMapper {
    public Map<Long, Long> getProductPriceMap(List<ProductEntity> products) {
        return products.stream().collect(
                Collectors.toMap(ProductEntity::getId, ProductEntity::getPrice)
        );
    }
}
