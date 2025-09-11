package com.loopers.domain.productmetric;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductMetricService {

    private final ProductMetricRegistry productMetricRegistry;

    @Transactional
    public ProductMetricEntity updateLikeCount(ProductMetricCommand.UpdateLikeCount command) {
        ProductMetricEntity entity = productMetricRegistry.findByProductId(command.productId())
                .orElseGet(()-> new ProductMetricEntity(command));
        entity.updateLikeCount(command);
        return productMetricRegistry.save(entity);
    }

    @Transactional
    public List<ProductMetricEntity> addSoldCount(List<ProductMetricCommand.AddSoldCount> commands) {
        List<ProductMetricEntity> entities = productMetricRegistry.findAllByProductIds(commands);

        for(ProductMetricCommand.AddSoldCount command : commands) {
            if(entities.stream().noneMatch(entity ->
                    entity.getProductId().equals(command.productId())
                    && entity.getMetricName().equals("sold_count"))
            ) {
                entities.add(new ProductMetricEntity(command));
            }
        }

        for(ProductMetricEntity entity : entities) {
            for(ProductMetricCommand.AddSoldCount command : commands) {
                if(entity.getProductId().equals(command.productId())
                        && entity.getMetricName().equals("sold_count")) {
                    entity.addSoldCount(command);
                }
            }
        }

        return productMetricRegistry.saveAll(entities);
    }

    public ProductMetricEntity addViewCount(ProductMetricCommand.AddViewCount command) {
        ProductMetricEntity entity = productMetricRegistry.findByProductId(command.productId())
                .orElseGet(()-> new ProductMetricEntity(command));
        entity.addViewCount(command);
        return productMetricRegistry.save(entity);
    }
}
