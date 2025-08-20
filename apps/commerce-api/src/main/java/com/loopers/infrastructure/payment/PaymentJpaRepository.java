package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentJpaRepository extends JpaRepository<PaymentOrderEntity, Long> {

    Optional<PaymentOrderEntity> findByOrderId(Long orderId);
}
