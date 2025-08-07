package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon")
@Entity
public class CouponEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private Type type;
    private Long value;
    private boolean usable;

    public CouponEntity(Type couponType, Long value) {
        if (couponType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입은 null일 수 없습니다.");
        }
        if (value == null || value < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 값은 null이거나 음수일 수 없습니다.");
        }
        couponType.validate(value);

        this.type = couponType;
        this.value = value;
        this.usable = true;
    }

    public enum Type {
        PERCENTAGE{
            @Override
            public Long calculate(Long price, Long value) {
                return price * value / 100;
            }
            @Override
            public void validate(Long value) {
                if (value < 0 || value > 100) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "퍼센트 값은 0 이상 100 이하이어야 합니다.");
                }
            }
        },
        FIXED{
            @Override
            public Long calculate(Long price, Long value) {
                return value;
            }
            @Override
            public void validate(Long value) {
                if (value < 0) {
                    throw new CoreException(ErrorType.BAD_REQUEST, "고정 값은 음수일 수 없습니다.");
                }
            }
        };
        public abstract Long calculate(Long price, Long value);
        public abstract void validate(Long value);
        public static Type from(String value) {
            if (value == null || value.isEmpty()) {
                throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰 타입은 null이거나 빈 문자열일 수 없습니다.");
            }
            try {
                return Type.valueOf(value);
            } catch (IllegalArgumentException e) {
                throw new CoreException(ErrorType.BAD_REQUEST, "유효하지 않은 쿠폰 타입입니다: " + value);
            }
        }
    }

    public Long getAppliedValue(Long price) {
        if(price == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 null일 수 없습니다.");
        }
        if(price < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "가격은 음수일 수 없습니다.");
        }
        if(!usable) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 사용 불가능 상태입니다.");
        }
        return type.calculate(price, value);
    }

    public CouponUsageEntity issue(Long userId) {
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용자 ID는 null일 수 없습니다.");
        }
        if (!usable) {
            throw new CoreException(ErrorType.BAD_REQUEST, "쿠폰이 사용 불가능 상태입니다.");
        }
        return new CouponUsageEntity(this, userId);
    }
}
