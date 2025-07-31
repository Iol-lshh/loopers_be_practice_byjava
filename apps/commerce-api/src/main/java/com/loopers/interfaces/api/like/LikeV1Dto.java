package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeResult;
import com.loopers.application.product.ProductResult;

import java.util.List;

public class LikeV1Dto {
    public record Response (
            Long userId,
            String targetType,
            Long targetId,
            Boolean isLike
    ){
        public static Response from(LikeResult.Result result) {
            return new Response(
                    result.userId(),
                    result.targetType(),
                    result.targetId(),
                    result.isLike()
            );
        }
    }

    public record ProductResponse (
            Long id,
            String name,
            Long brandId,
            String brandName,
            Long price,
            Long stock,
            Long likeCount,
            String state,
            String releasedAt
    ){
        public static List<ProductResponse> from(List<ProductResult.Summary> result) {
            return result.stream()
                    .map(product -> new ProductResponse(
                            product.id(),
                            product.name(),
                            product.brandId(),
                            product.brandName(),
                            product.price(),
                            product.stock(),
                            product.likeCount(),
                            product.state(),
                            product.releasedAt()
                    )).toList();
        }
    }
}
