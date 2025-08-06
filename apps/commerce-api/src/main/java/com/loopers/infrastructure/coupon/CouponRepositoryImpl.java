package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponEntity;
import com.loopers.domain.coupon.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;

    @Override
    public CouponEntity save(CouponEntity coupon) {
        return couponJpaRepository.save(coupon);
    }

    @Override
    public Optional<CouponEntity> find(Long id) {
        return couponJpaRepository.findById(id);
    }

    @Override
    public List<CouponEntity> find(List<Long> ids) {
        return couponJpaRepository.findAllById(ids);
    }
}
