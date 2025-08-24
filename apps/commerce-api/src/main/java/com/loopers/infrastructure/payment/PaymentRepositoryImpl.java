package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentRepositoryImpl implements PaymentRepository {
    private final PaymentJpaRepository jpaRepository;

    @Override
    public PaymentEntity save(PaymentEntity payment) {
        return jpaRepository.save(payment);
    }

    @Override
    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return jpaRepository.findByOrderId(orderId);
    }

    @Override
    public PaymentEntity saveAndFlush(PaymentEntity payment) {
        return jpaRepository.saveAndFlush(payment);
    }

    @Override
    public Optional<PaymentEntity> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<PaymentEntity> findByOrderKey(String orderKey) {
        return jpaRepository.findByOrderKey(orderKey);
    }
}
