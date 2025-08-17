package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {
    private Long orderId;
    private Long userId;
    private Long amount;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "payment")
    private List<PaymentCouponEntity> coupons;

    public static PaymentEntity of(
            Long orderId,
            Long userId,
            Long amount
    ) {
        PaymentEntity payment = new PaymentEntity();
        payment.orderId = orderId;
        payment.userId = userId;
        payment.amount = amount;
        return payment;
    }

    public enum TransactionStatus {
        PENDING,
        SUCCESS,
        FAILED
    }
}
