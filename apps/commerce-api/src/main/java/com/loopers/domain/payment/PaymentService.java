package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PgClient pgClient;
    private final UserCardRepository userCardRepository;

    public PaymentEntity pay(PaymentCommand.Pay command) {
        Optional<PgInfo.TransactionStatus> info = pgClient.find(command.orderId());
        PaymentEntity payment = command.toEntity();
        return paymentRepository.save(payment);
    }

    public void request(PaymentCommand.Request requestCommand) {
        UserCardEntity cardEntity = userCardRepository.find(requestCommand.userId())
                .orElseThrow(() -> new CoreException(
                        ErrorType.BAD_REQUEST, "사용자의 카드 정보를 찾을 수 없습니다. userId: " + requestCommand.userId()));

        // todo 재처리, 취소, timeout, 서킷브레이커 처리
        PgStatement.Request requestStatement = new PgStatement.Request(
                requestCommand.orderId(),
                requestCommand.totalPrice(),
                cardEntity.getCardNumber(),
                cardEntity.getCardType()
        );
        PgInfo.TransactionStatus pgInfo = pgClient.request(requestStatement);
    }

    public UserCardEntity registerCard(UserCardCommand.Register command) {
        UserCardEntity userCardEntity = command.toEntity();
        return userCardRepository.save(userCardEntity);
    }
}
