package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {
    private Long orderId;
    private Long userId;
    private Long amount;
    @Enumerated(EnumType.STRING)
    private Type type;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "payment")
    private List<PaymentCouponEntity> coupons;

    public static PaymentEntity of(
            Long orderId,
            Long userId,
            Long amount,
            String type,
            Map<Long, Long> couponMap
    ) {
        PaymentEntity payment = new PaymentEntity();
        payment.orderId = orderId;
        payment.userId = userId;
        payment.amount = amount;
        payment.type = Type.valueOf(type);
        return payment;
    }

    public enum Type {
        POINT
    }
}
