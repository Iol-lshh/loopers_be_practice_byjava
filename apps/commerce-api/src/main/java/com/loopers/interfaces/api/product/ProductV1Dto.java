package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductResult;

import java.util.List;

public class ProductV1Dto {

    public record DetailResponse(
            Long id,
            String name,
            Long brandId,
            String brandName,
            Long price,
            Long stock,
            Long likeCount,
            String state,
            String releasedAt
    ) {
        public static DetailResponse from(ProductResult.Detail info) {
            return new DetailResponse(
                info.id(),
                info.name(),
                info.brandId(),
                info.brandName(),
                info.price(),
                info.stock(),
                info.likeCount(),
                info.state(),
                info.releasedAt()
            );
        }
    }

    public record SummaryResponse(
            Long id,
            String name,
            Long brandId,
            String brandName,
            Long price,
            Long stock,
            Long likeCount,
            String state,
            String releasedAt
    ) {
        public static SummaryResponse from(ProductResult.Summary info) {
            return new SummaryResponse(
                info.id(),
                info.name(),
                info.brandId(),
                info.brandName(),
                info.price(),
                info.stock(),
                info.likeCount(),
                info.state(),
                info.releasedAt()
            );
        }

        public static List<SummaryResponse> of(List<ProductResult.Summary> result) {
            return result.stream()
                    .map(SummaryResponse::from)
                    .toList();
        }
    }
}
