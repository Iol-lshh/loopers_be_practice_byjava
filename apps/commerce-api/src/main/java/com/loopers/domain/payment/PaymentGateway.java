package com.loopers.domain.payment;

import java.util.Optional;

public interface PaymentGateway {
    Optional<PaymentInfo.Order> findOrder(Long userId, String orderKey);

    Optional<PaymentInfo.Transaction> findTransaction(Long userId, String transactionKey);

    PaymentInfo.Transaction request(PaymentStatement.Request requestStatement);
}
