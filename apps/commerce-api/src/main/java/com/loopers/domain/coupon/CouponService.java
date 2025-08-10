package com.loopers.domain.coupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CouponEntity register(CouponCommand.Admin.Create command) {
        CouponEntity coupon = command.toEntity();
        return couponRepository.save(coupon);
    }

    public Optional<CouponEntity> find(Long id) {
        return couponRepository.find(id);
    }

    public List<CouponEntity> find(List<Long> ids) {
        return couponRepository.find(ids);
    }

    @Transactional
    public Map<Long, Long> getCouponValueMap(
            CouponCommand.User.Order couponCommand, Map<Long, Long> orderItemMap, Map<Long, Long> productPriceMap
    ) {
        List<CouponEntity> coupons = couponRepository.findWithLock(couponCommand.couponIds());
        if(coupons.size() != couponCommand.couponIds().size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다: " + couponCommand.couponIds());
        }
        if(couponRepository.existsUsages(couponCommand.userId(), couponCommand.couponIds())){
            throw new CoreException(ErrorType.CONFLICT, "이미 사용된 쿠폰이 있습니다");
        }
        Long totalPrice = orderItemMap.entrySet()
                .stream()
                .mapToLong(entry ->
                        entry.getValue() * productPriceMap.getOrDefault(entry.getKey(), 0L))
                .sum();
        return coupons.stream()
                .collect(Collectors.toMap(
                        CouponEntity::getId,
                        coupon -> coupon.getAppliedValue(totalPrice),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new
                ));
    }

    @Transactional
    public List<CouponEntity> useCoupons(Long userId, List<Long> couponIds) {
        List<CouponEntity> coupons = couponRepository.findWithLock(couponIds);
        if (coupons.size() != couponIds.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "쿠폰이 존재하지 않습니다: " + couponIds);
        }
        if(couponRepository.existsUsages(userId, couponIds)) {
            throw new CoreException(ErrorType.CONFLICT, "이미 사용된 쿠폰이 있습니다");
        }
        List<CouponUsageEntity> usages = coupons.stream()
                .map(coupon -> coupon.issue(userId))
                .toList();
        couponRepository.saveUsages(usages);
        return coupons;
    }
}
