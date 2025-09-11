package com.loopers.domain.productmetric;

public class ProductMetricEvent {

    public record UpdateLikeCount(
            Long productId,
            Long likeCount
    ){
            public static UpdateLikeCount from(ProductMetricEntity entity) {
                return new UpdateLikeCount(
                        entity.getProductId(),
                        Long.parseLong(entity.getMetricValue())
                );
            }
    }

    public record UpdateSoldCount(
            Long productId,
            Long soldCount
    ){
            public static UpdateSoldCount from(ProductMetricEntity entity) {
                return new UpdateSoldCount(
                        entity.getProductId(),
                        Long.parseLong(entity.getMetricValue())
                );
            }
    }
}
