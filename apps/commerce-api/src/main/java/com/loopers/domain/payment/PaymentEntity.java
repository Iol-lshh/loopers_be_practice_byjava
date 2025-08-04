package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static PaymentEntity of(
            Long orderId,
            Long userId,
            Long amount,
            String type
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
