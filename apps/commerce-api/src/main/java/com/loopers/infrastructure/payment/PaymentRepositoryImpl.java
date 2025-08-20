package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentOrderEntity;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;

    @Override
    public PaymentOrderEntity save(PaymentOrderEntity payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentOrderEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public PaymentOrderEntity saveAndFlush(PaymentOrderEntity payment) {
        return jpaRepository.saveAndFlush(payment);
    }
}
