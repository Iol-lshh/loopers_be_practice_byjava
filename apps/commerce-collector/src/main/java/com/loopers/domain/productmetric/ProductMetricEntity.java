package com.loopers.domain.productmetric;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product_metrics")
public class ProductMetricEntity {
    @Id
    private Long id;
    private Long productId;
    private String metricName;
    private String metricValue;

    public ProductMetricEntity(ProductMetricCommand.UpdateLikeCount command) {
        this.productId = command.productId();
        this.metricName = "like_count";
        this.metricValue = String.valueOf(command.likeCount());
    }

    public void updateLikeCount(ProductMetricCommand.UpdateLikeCount command) {
        this.metricValue = String.valueOf(command.likeCount());
    }
}
