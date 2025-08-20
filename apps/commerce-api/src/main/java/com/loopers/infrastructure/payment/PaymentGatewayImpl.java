package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentGatewayImpl implements PaymentGateway {
    private final PgV1FeignClient pgV1FeignClient;
    private final static String CALLBACK_URL = "http://localhost:8080/api/v1/payment/pay";

    @Override
    public Optional<PaymentInfo.Order> findOrder(Long userId, String orderKey) {
        var response = pgV1FeignClient.findOrder(orderKey, String.valueOf(userId));
        if(response.data() == null) return Optional.empty();
        PgV1Dto.Response.Order dto = response.data();
        return Optional.of(dto.getInfo());
    }

    @Override
    public Optional<PaymentInfo.Transaction> findTransaction(Long userId, String transactionKey) {
        var response = pgV1FeignClient.findTransaction(transactionKey, String.valueOf(userId));
        if (response.data() == null) return Optional.empty();
        PgV1Dto.Response.Transaction dto = response.data();
        return Optional.of(dto.getInfo());
    }

    @Override
    public PaymentInfo.Transaction request(PaymentStatement.Request requestStatement) {
        PgV1Dto.Request.Transaction request = new PgV1Dto.Request.Transaction(
                requestStatement.orderKey(),
                requestStatement.cardType(),
                requestStatement.cardNumber(),
                requestStatement.totalPrice(),
                CALLBACK_URL
        );
        var response = pgV1FeignClient.request(String.valueOf(requestStatement.userId()), request);
        return response.data().getInfo();
    }
}
