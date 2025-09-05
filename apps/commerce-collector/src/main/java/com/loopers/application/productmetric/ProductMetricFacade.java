package com.loopers.application.productmetric;

import com.loopers.domain.productmetric.ProductMetricCommand;
import com.loopers.domain.productmetric.ProductMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ProductMetricFacade {
    private final ProductMetricService productMetricService;

    public void update(ProductMetricCommand.UpdateLikeCount command) {
        productMetricService.updateLikeCount(command);
    }
}
