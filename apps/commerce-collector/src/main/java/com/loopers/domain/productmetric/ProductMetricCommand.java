package com.loopers.domain.productmetric;

public class ProductMetricCommand {
    public record UpdateLikeCount(
            Long productId,
            Long likeCount
    ){
    }

    public record AddSoldCount(
            Long productId,
            Long soldCount
    ){
    }

    public record AddViewCount(
            Long productId,
            Long viewCount
    ){
    }
}
