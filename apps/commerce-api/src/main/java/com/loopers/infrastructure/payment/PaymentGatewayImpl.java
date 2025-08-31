package com.loopers.infrastructure.payment;

import com.loopers.domain.payment.PaymentGateway;
import com.loopers.domain.payment.PaymentInfo;
import com.loopers.domain.payment.PaymentStatement;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentGatewayImpl implements PaymentGateway {
    private final PgV1FeignClient pgV1FeignClient;
    private final static String CALLBACK_URL = "http://localhost:8080/api/v1/payments/transactions";

    @Override
    public Optional<PaymentInfo.Order> findOrder(Long userId, String orderKey) {
        try{
            var response = pgV1FeignClient.findOrder(orderKey, String.valueOf(userId));
            if(response.data() == null) return Optional.empty();
            PgV1Dto.Response.Order dto = response.data();
            return Optional.of(dto.getInfo());
        } catch (CallNotPermittedException e) {
            log.error("PG findOrder 서킷브레이커 오픈", e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 서비스가 현재 사용 불가능합니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Override
    public Optional<PaymentInfo.Transaction> findTransaction(Long userId, String transactionKey) {
        try{
            var response = pgV1FeignClient.findTransaction(transactionKey, String.valueOf(userId));
            if (response.data() == null) return Optional.empty();
            PgV1Dto.Response.Transaction dto = response.data();
            return Optional.of(dto.getInfo());
        } catch (CallNotPermittedException e) {
            log.error("PG findTransaction 서킷브레이커 오픈", e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 서비스가 현재 사용 불가능합니다. 잠시 후 다시 시도해주세요.");
        }
    }

    @Override
    public PaymentInfo.Transaction request(PaymentStatement.Request requestStatement) {
        log.info("PG 결제 진행 요청 - orderKey: {}, userId: {}, amount: {}, cardType: {}",
                requestStatement.orderKey(), requestStatement.userId(), requestStatement.totalPrice(), requestStatement.cardType());
        PgV1Dto.Request.Transaction request = new PgV1Dto.Request.Transaction(
                requestStatement.orderKey(),
                requestStatement.cardType(),
                requestStatement.cardNumber(),
                requestStatement.totalPrice(),
                CALLBACK_URL
        );
        try{
            var response = pgV1FeignClient.request(String.valueOf(requestStatement.userId()), request);
            log.info("PG 결제 진행 요청 성공 - transactionKey: {}, status: {}, reason: {}",
                    response.data().transactionKey(), response.data().status(), response.data().reason());
            return response.data().getInfo();
        } catch (CallNotPermittedException e) {
            log.error("PG request 서킷브레이커 오픈", e);
            throw new CoreException(ErrorType.INTERNAL_ERROR, "PG 서비스가 현재 사용 불가능합니다. 잠시 후 다시 시도해주세요.");
        }
    }
}
