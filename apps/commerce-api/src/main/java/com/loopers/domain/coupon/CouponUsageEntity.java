package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "coupon_usage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CouponUsageEntity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "coupon_id", nullable = false)
    private CouponEntity coupon;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    public CouponUsageEntity(CouponEntity coupon, Long userId) {
        if(coupon == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰은 null일 수 없습니다.");
        }
        if(userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 null일 수 없습니다.");
        }
        this.coupon = coupon;
        this.userId = userId;
    }
}
