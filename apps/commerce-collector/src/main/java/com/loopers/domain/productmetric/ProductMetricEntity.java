package com.loopers.domain.productmetric;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_metrics")
public class ProductMetricEntity extends BaseEntity {
    private Long productId;
    private String metricName;
    private String metricValue;

    public ProductMetricEntity(ProductMetricCommand.UpdateLikeCount command) {
        this.productId = command.productId();
        this.metricName = "like_count";
        this.metricValue = String.valueOf(command.likeCount());
    }

    public ProductMetricEntity(ProductMetricCommand.AddSoldCount command) {
        this.productId = command.productId();
        this.metricName = "sold_count";
        this.metricValue = "0";
    }

    public ProductMetricEntity(ProductMetricCommand.AddViewCount command) {
        this.productId = command.productId();
        this.metricName = "view_count";
        this.metricValue = "0";
    }

    public void updateLikeCount(ProductMetricCommand.UpdateLikeCount command) {
        if(!this.metricName.equals("like_count")) {
            throw new IllegalArgumentException("like_count 메트릭이 아닙니다.");
        }
        this.metricValue = String.valueOf(command.likeCount());
    }

    public void addSoldCount(ProductMetricCommand.AddSoldCount command) {
        if(!this.metricName.equals("sold_count")) {
            throw new IllegalArgumentException("sold_count 메트릭이 아닙니다.");
        }
        Long currentSoldCount = Long.parseLong(this.metricValue);
        this.metricValue = String.valueOf(currentSoldCount + command.soldCount());
    }

    public void addViewCount(ProductMetricCommand.AddViewCount command) {
        if(!this.metricName.equals("veiw_count")) {
            throw new IllegalArgumentException("view_count 메트릭이 아닙니다.");
        }
        Long currentViewCount = Long.parseLong(this.metricValue);
        this.metricValue = String.valueOf(currentViewCount + command.viewCount());
    }
}
