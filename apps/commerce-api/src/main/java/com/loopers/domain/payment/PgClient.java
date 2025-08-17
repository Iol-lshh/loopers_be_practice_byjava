package com.loopers.domain.payment;

import java.util.Optional;

public interface PgClient {
    Optional<PgInfo.TransactionStatus> find(Long orderId);

    PgInfo.TransactionStatus request(PgStatement.Request requestStatement);
}
