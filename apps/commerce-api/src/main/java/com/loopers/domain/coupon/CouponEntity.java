package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
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

    public CouponEntity(Type couponType, Long value) {
        this.type = couponType;
        this.value = value;
    }

    public enum Type {
        PERCENTAGE{
            @Override
            public Long calculate(Long price, Long value) {
                return price * value / 100;
            }
        },
        FIXED{
            @Override
            public Long calculate(Long price, Long value) {
                return value;
            }
        };
        public abstract Long calculate(Long price, Long value);
    }

    public Long getAppliedValue(Long price) {
        return type.calculate(price, value);
    }
}
