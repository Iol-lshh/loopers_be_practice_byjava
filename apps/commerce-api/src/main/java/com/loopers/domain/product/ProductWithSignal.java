package com.loopers.domain.product;

import lombok.Getter;
import org.springframework.data.annotation.Immutable;

import java.time.ZonedDateTime;

@Immutable
@Getter
public class ProductWithSignal {
    private Long id;
    private String name;
    private Long brandId;
    private Long price;
    private Long stock;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
    private ProductEntity.State state;
    private Long likeCount;

    public ProductWithSignal(ProductEntity product,
                             Long likeCount) {
        this.id = product.getId();
        this.name = product.getName();
        this.brandId = product.getBrandId();
        this.price = product.getPrice();
        this.stock = product.getStock();
        this.createdAt = product.getCreatedAt();
        this.updatedAt = product.getUpdatedAt();
        this.state = product.getState();
        this.likeCount = likeCount != null ? likeCount : 0L;
    }
}
