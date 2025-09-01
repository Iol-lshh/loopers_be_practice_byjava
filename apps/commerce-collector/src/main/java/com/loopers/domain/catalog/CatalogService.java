package com.loopers.domain.catalog;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CatalogService {
    private final CatalogCacheRepository catalogCacheRepository;

    public void consumeOutOfStock(Long productId) {
        catalogCacheRepository.evict(productId);
    }
}
