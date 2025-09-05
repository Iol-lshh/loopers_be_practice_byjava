package com.loopers.domain.productmetric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductMetricService {

    private final ProductMetricRegistry productMetricRegistry;

    public ProductMetricEntity updateLikeCount(ProductMetricCommand.UpdateLikeCount command) {
        var entity = productMetricRegistry.findByProductId(command.productId())
                .orElseGet(()-> new ProductMetricEntity(command));
        entity.updateLikeCount(command);
        return productMetricRegistry.save(entity);
    }
}
