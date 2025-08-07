package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsageEntity, Long> {

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END " +
            "FROM CouponUsageEntity c WHERE c.userId = :userId AND c.coupon.id IN :couponIds")
    boolean existsAny(Long userId, List<Long> couponIds);
}
