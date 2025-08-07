package com.loopers.domain.payment;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class PaymentService {
    private final PaymentRepository repository;
    private final Map<PaymentWay.Type, PaymentWay<?>> paymentWayMap;


    public PaymentService(PaymentRepository paymentRepository, List<PaymentWay<?>> paymentWays) {
        this.repository = paymentRepository;
        this.paymentWayMap = paymentWays.stream()
                .collect(Collectors.toMap(PaymentWay::getType, paymentWay -> paymentWay));
    }

    public PaymentEntity pay(PaymentCommand.Pay command) {
        PaymentWay.Type paymentType = PaymentWay.Type.of(command.paymentType());
        PaymentWay<?> service = paymentWayMap.get(paymentType);
        service.pay(command.userId(), command.totalPrice());
        PaymentEntity payment = command.toEntity();
        return repository.save(payment);
    }
}
