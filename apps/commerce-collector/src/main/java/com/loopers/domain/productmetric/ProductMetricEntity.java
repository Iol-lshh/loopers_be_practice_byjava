package com.loopers.domain.productmetric;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product_metrics")
public class ProductMetricEntity extends BaseEntity {
    private Long productId;
    private String metricName;
    private String metricValue;
    private LocalDate metricDate;

    public ProductMetricEntity(ProductMetricCommand.UpdateLikeCount command) {
        this.productId = command.productId();
        this.metricName = "like_count";
        this.metricValue = String.valueOf(command.likeCount());
        this.metricDate = LocalDate.now();
    }

    public ProductMetricEntity(ProductMetricCommand.AddSoldCount command) {
        this.productId = command.productId();
        this.metricName = "sold_count";
        this.metricValue = String.valueOf(command.soldCount());
        this.metricDate = LocalDate.now();
    }

    public ProductMetricEntity(ProductMetricCommand.AddSoldAmount command) {
        this.productId = command.productId();
        this.metricName = "sold_amount";
        this.metricValue = String.valueOf(command.soldAmount());
        this.metricDate = LocalDate.now();
    }

    public ProductMetricEntity(ProductMetricCommand.AddViewCount command) {
        this.productId = command.productId();
        this.metricName = "view_count";
        this.metricValue = String.valueOf(command.viewCount());
        this.metricDate = LocalDate.now();
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

    public void addSoldAmount(ProductMetricCommand.AddSoldAmount command) {
        if(!this.metricName.equals("sold_amount")) {
            throw new IllegalArgumentException("sold_amount 메트릭이 아닙니다.");
        }
        Long currentSoldAmount = Long.parseLong(this.metricValue);
        this.metricValue = String.valueOf(currentSoldAmount + command.soldAmount());
    }
}
