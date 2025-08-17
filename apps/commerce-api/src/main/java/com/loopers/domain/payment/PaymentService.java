package com.loopers.domain.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentService {
    private final PaymentRepository repository;

    public PaymentEntity pay(PaymentCommand.Pay command) {
        PaymentEntity payment = command.toEntity();
        return repository.save(payment);
    }
}
