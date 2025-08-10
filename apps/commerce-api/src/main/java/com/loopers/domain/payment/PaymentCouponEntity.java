package com.loopers.domain.payment;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Entity
@Table(name = "payment_coupon")
@IdClass(PaymentCouponEntity.PaymentCouponId.class)
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentCouponEntity {
    @Id
    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity payment;
    @Id
    private Long couponId;
    private Long discountPrice;

    public PaymentCouponEntity(Long couponId, Long discountPrice, PaymentEntity payment) {
        this.couponId = couponId;
        this.discountPrice = discountPrice;
        this.payment = payment;
    }

    public static PaymentCouponEntity of(Long couponId, Long discountPrice, PaymentEntity payment) {
        return new PaymentCouponEntity(couponId, discountPrice, payment);
    }

    @Embeddable
    @NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
    @EqualsAndHashCode
    public static class PaymentCouponId implements Serializable {
        private Long payment;
        private Long couponId;
    }
}
