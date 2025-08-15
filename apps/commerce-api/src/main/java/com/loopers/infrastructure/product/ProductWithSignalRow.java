package com.loopers.infrastructure.product;

import java.time.LocalDateTime;

public interface ProductWithSignalRow {
    Long getId();
    Long getBrandId();
    LocalDateTime getCreatedAt();
    String getName();
    Long getPrice();
    LocalDateTime getReleasedAt();
    String getState();
    Long getStock();
    LocalDateTime getUpdatedAt();
    Long getLikeCount();
}
