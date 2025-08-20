package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentRepository {
    PaymentOrderEntity save(PaymentOrderEntity payment);

    Optional<PaymentOrderEntity> findByOrderId(Long orderId);

    PaymentOrderEntity saveAndFlush(PaymentOrderEntity payment);
}
