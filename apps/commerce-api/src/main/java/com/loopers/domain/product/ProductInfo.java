package com.loopers.domain.product;

import com.loopers.infrastructure.product.ProductWithSignalRow;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Immutable;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public class ProductInfo {
    @Getter
    @Immutable
    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    public static class ProductWithSignal {
        private Long id;
        private String name;
        private Long brandId;
        private Long price;
        private Long stock;
        private ZonedDateTime createdAt;
        private ZonedDateTime updatedAt;
        private ProductEntity.State state;
        private Long likeCount;

        public ProductWithSignal(ProductEntity productEntity, Long likeCount) {
            this.id = productEntity.getId();
            this.name = productEntity.getName();
            this.brandId = productEntity.getBrandId();
            this.price = productEntity.getPrice();
            this.stock = productEntity.getStock();
            this.createdAt = productEntity.getCreatedAt();
            this.updatedAt = productEntity.getUpdatedAt();
            this.state = productEntity.getState();
            this.likeCount = likeCount != null ? likeCount : 0L;
        }

        protected ProductWithSignal(
                Long id,
                Long brandId,
                LocalDateTime createdAt,
                String name,
                Long price,
                LocalDateTime releasedAt,
                String state,
                Long stock,
                LocalDateTime updatedAt,
                Long likeCount
        ) {
            this.id = id;
            this.name = name;
            this.brandId = brandId;
            this.price = price;
            this.stock = stock;
            this.createdAt = createdAt != null ? createdAt.atZone(java.time.ZoneId.of("Asia/Seoul")) : null;
            this.updatedAt = updatedAt != null ? updatedAt.atZone(java.time.ZoneId.of("Asia/Seoul")) : null;
            var _releasedAt = releasedAt != null ? releasedAt.atZone(java.time.ZoneId.of("Asia/Seoul")) : null;
            this.state = ProductEntity.State.of(state, _releasedAt);
            this.likeCount = likeCount != null ? likeCount : 0L;
        }

        public static ProductWithSignal from(ProductWithSignalRow productWithSignalRow) {
            return new ProductWithSignal(
                    productWithSignalRow.getId(),
                    productWithSignalRow.getBrandId(),
                    productWithSignalRow.getCreatedAt(),
                    productWithSignalRow.getName(),
                    productWithSignalRow.getPrice(),
                    productWithSignalRow.getReleasedAt(),
                    productWithSignalRow.getState(),
                    productWithSignalRow.getStock(),
                    productWithSignalRow.getUpdatedAt(),
                    productWithSignalRow.getLikeCount()
            );
        }
    }
}
