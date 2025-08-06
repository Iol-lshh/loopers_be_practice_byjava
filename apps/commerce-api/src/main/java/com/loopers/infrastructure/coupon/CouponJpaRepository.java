package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {


}
