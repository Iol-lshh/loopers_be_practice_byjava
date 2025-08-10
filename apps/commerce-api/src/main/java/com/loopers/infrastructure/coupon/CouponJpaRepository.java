package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, Long> {


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CouponEntity c WHERE c.id IN :ids")
    List<CouponEntity> lockByIds(List<Long> ids);
}
