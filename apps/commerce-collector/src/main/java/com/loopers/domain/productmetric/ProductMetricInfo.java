package com.loopers.domain.productmetric;

public class ProductMetricInfo {
    public record Aggregate(
        Long productId,
        Long viewCount,
        Long likeCount,
        Long soldCount,
        Long soldAmount
    ){
    }
}
