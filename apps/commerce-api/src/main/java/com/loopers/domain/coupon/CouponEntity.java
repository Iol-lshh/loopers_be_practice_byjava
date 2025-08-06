package com.loopers.domain.coupon;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coupon")
@Entity
public class CouponEntity extends BaseEntity {

    private Type type;
    private Long value;
    @Version private Long version;

    public CouponEntity(Type couponType, Long value) {
        super();
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
