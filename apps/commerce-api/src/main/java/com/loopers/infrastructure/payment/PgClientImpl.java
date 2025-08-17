package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PgClient;
import com.loopers.domain.payment.PgInfo;
import com.loopers.domain.payment.PgStatement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PgClientImpl implements PgClient {

    @Override
    public Optional<PgInfo.TransactionStatus> find(Long orderId) {
        // todo
        return Optional.empty();
    }

    @Override
    public PgInfo.TransactionStatus request(PgStatement.Request requestStatement) {
        // todo
        return null;
    }
}
