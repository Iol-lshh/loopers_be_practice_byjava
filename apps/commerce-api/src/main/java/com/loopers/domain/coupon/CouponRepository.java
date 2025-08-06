package com.loopers.domain.coupon;

import java.util.List;
import java.util.Optional;

public interface CouponRepository {
    CouponEntity save(CouponEntity coupon);

    Optional<CouponEntity> find(Long id);

    List<CouponEntity> find(List<Long> ids);
}
