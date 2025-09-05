package com.loopers.domain.productmetric;

public class ProductMetricCommand {
    public record UpdateLikeCount(
            Long productId,
            Long likeCount
    ){
    }
}
