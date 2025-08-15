package com.loopers.application.product;


import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.like.LikeSummaryEntity;
import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductInfo;

import java.util.*;

public class ProductResult {
    public record Summary(
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
        private static class SummaryBuilder {
            private Long id;
            private String name;
            private Long brandId;
            private String brandName;
            private Long price;
            private Long stock;
            private Long likeCount;
            private String state;
            private String releasedAt;
        }

        public static List<Summary> of(List<BrandEntity> brandList, List<ProductInfo.ProductWithSignal> productSignalList) {
            SequencedMap<Long, SummaryBuilder> sbList = new LinkedHashMap<>();
            for(ProductInfo.ProductWithSignal productSignal: productSignalList) {
                SummaryBuilder sb = new SummaryBuilder();
                sb.id = productSignal.getId();
                sb.name = productSignal.getName();
                sb.brandId = productSignal.getBrandId();
                sb.price = productSignal.getPrice();
                sb.stock = productSignal.getStock();
                sb.likeCount = productSignal.getLikeCount();
                sb.state = productSignal.getState().getValue().name();
                sb.releasedAt = productSignal.getState().getReleasedAt() != null ? productSignal.getState().getReleasedAt().toString() : "";
                sbList.put(productSignal.getId(), sb);
            }
            for(BrandEntity brand: brandList) {
                SummaryBuilder sb = sbList.get(brand.getId());
                if (sb != null) {
                    sb.brandName = brand.getName();
                }
            }
            return sbList.values().stream()
                    .map(sb -> new Summary(
                            sb.id,
                            sb.name,
                            sb.brandId,
                            sb.brandName,
                            sb.price,
                            sb.stock,
                            sb.likeCount,
                            sb.state,
                            sb.releasedAt
                    )).toList();
        }
    }

    public record Detail(
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
        public static Detail of(BrandEntity brand, ProductInfo.ProductWithSignal productWithSignal) {
            return new Detail(
                    productWithSignal.getId(),
                    productWithSignal.getName(),
                    productWithSignal.getBrandId(),
                    brand.getName(),
                    productWithSignal.getPrice(),
                    productWithSignal.getStock(),
                    productWithSignal.getLikeCount(),
                    productWithSignal.getState().getValue().name(),
                    productWithSignal.getState().getReleasedAt() != null ? productWithSignal.getState().getReleasedAt().toString() : ""
            );
        }

        public static Detail of(BrandEntity brand, ProductEntity releasedProduct, LikeSummaryEntity likeSummary) {
            return new Detail(
                    releasedProduct.getId(),
                    releasedProduct.getName(),
                    releasedProduct.getBrandId(),
                    brand.getName(),
                    releasedProduct.getPrice(),
                    releasedProduct.getStock(),
                    likeSummary != null ? likeSummary.getLikeCount() : 0L,
                    releasedProduct.getState().getValue().name(),
                    releasedProduct.getState().getReleasedAt() != null ? releasedProduct.getState().getReleasedAt().toString() : ""
            );
        }
    }
}
