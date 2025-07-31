package com.loopers.domain.brand;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    Optional<BrandEntity> find(Long brandId);

    BrandEntity save(BrandEntity brand);

    List<BrandEntity> find(List<Long> ids);
}
