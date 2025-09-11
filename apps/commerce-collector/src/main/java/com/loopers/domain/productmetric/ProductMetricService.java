package com.loopers.domain.productmetric;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductMetricService {

    private final ProductMetricRegistry productMetricRegistry;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ProductMetricEntity updateLikeCount(ProductMetricCommand.UpdateLikeCount command) {
        ProductMetricEntity entity = productMetricRegistry.findByProductIdAndMetric(command.productId(), "like_count")
                .orElseGet(()-> new ProductMetricEntity(command));
        entity.updateLikeCount(command);
        return productMetricRegistry.save(entity);
    }

    @Transactional
    public List<ProductMetricEntity> addSoldCount(List<ProductMetricCommand.AddSoldCount> countComands, List<ProductMetricCommand.AddSoldAmount> amountCommands) {
        List<ProductMetricEntity> countEntities = productMetricRegistry.findAllByProductIdsAndMetric(countComands.stream().map(ProductMetricCommand.AddSoldCount::productId).toList(), "sold_count");
        List<ProductMetricEntity> amountEntities = productMetricRegistry.findAllByProductIdsAndMetric(amountCommands.stream().map(ProductMetricCommand.AddSoldAmount::productId).toList(), "sold_amount");


        for(ProductMetricCommand.AddSoldCount command : countComands) {
            if(countEntities.stream().noneMatch(entity ->
                    entity.getProductId().equals(command.productId())
                    && entity.getMetricName().equals("sold_count"))
            ) {
                countEntities.add(new ProductMetricEntity(command));
            }
        }

        for(ProductMetricCommand.AddSoldAmount command : amountCommands) {
            if(amountEntities.stream().noneMatch(entity ->
                    entity.getProductId().equals(command.productId())
                            && entity.getMetricName().equals("sold_amount"))
            ) {
                amountEntities.add(new ProductMetricEntity(command));
            }
        }

        for(ProductMetricEntity entity : countEntities) {
            for(ProductMetricCommand.AddSoldCount command : countComands) {
                if(entity.getProductId().equals(command.productId())
                        && entity.getMetricName().equals("sold_count")) {
                    entity.addSoldCount(command);
                }
            }
        }

        for(ProductMetricEntity entity : amountEntities) {
            for(ProductMetricCommand.AddSoldAmount command : amountCommands) {
                if(entity.getProductId().equals(command.productId())
                        && entity.getMetricName().equals("sold_amount")) {
                    entity.addSoldAmount(command);
                }
            }
        }

        List<ProductMetricEntity> allEntities = new ArrayList<>();
        allEntities.addAll(countEntities);
        allEntities.addAll(amountEntities);

        return productMetricRegistry.saveAll(allEntities);
    }

    public ProductMetricEntity addViewCount(ProductMetricCommand.AddViewCount command) {
        ProductMetricEntity entity = productMetricRegistry.findByProductIdAndMetric(command.productId(), "view_count")
                .orElseGet(()-> new ProductMetricEntity(command));
        entity.addViewCount(command);
        return productMetricRegistry.save(entity);
    }

    public Optional<ProductMetricInfo.Aggregate> findAggregate(Long productId) {
        return productMetricRegistry.findAggregate(productId);
    }

    public List<Long> findAllDistinctProductIds() {
        return productMetricRegistry.findAllDistinctProductIds();
    }
}
