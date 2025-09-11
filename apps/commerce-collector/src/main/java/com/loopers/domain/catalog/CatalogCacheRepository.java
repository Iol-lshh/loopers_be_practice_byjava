package com.loopers.domain.catalog;

import org.springframework.stereotype.Component;

@Component
public interface CatalogCacheRepository {

    void evict(Long aLong);
}
