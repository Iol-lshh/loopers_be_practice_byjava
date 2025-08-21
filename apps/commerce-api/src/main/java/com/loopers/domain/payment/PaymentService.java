package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final UserCardRepository userCardRepository;

    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    public PaymentEntity pay(PaymentCommand.Transaction command) {
        PaymentInfo.Order orderInfo = paymentGateway.findOrder(command.userId(), command.orderKey()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다. userId: " + command.userId() + ", orderId: " + command.orderId()
        ));
        PaymentEntity payment = paymentRepository.findByOrderId(command.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 주문을 찾을 수 없습니다. orderId: " + command.orderId()
        ));
        payment.update(orderInfo);
        return paymentRepository.saveAndFlush(payment);
    }

    public void request(PaymentCommand.Request requestCommand) {
        UserCardEntity cardEntity = userCardRepository.findByUserId(requestCommand.userId()).orElseThrow(() -> new CoreException(
                ErrorType.BAD_REQUEST, "사용자의 카드 정보를 찾을 수 없습니다. userId: " + requestCommand.userId())
        );
        PaymentEntity payment = paymentRepository.findByOrderId(requestCommand.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 주문을 찾을 수 없습니다. orderId: " + requestCommand.orderId()
        ));

        PaymentStatement.Request requestStatement = new PaymentStatement.Request(
                payment.getOrderKey(),
                payment.getUserId(),
                payment.getAmount(),
                cardEntity.getCardNumber(),
                cardEntity.getCardType()
        );
        PaymentInfo.Transaction result = paymentGateway.request(requestStatement);
        payment.updateTransaction(result);
        paymentRepository.saveAndFlush(payment);
    }

    public UserCardEntity registerCard(UserCardCommand.Register command) {
        UserCardEntity userCardEntity = command.toEntity();
        return userCardRepository.save(userCardEntity);
    }

    public PaymentEntity register(PaymentCommand.RegisterOrder paymentCommand) {
        PaymentEntity paymentEntity = PaymentEntity.from(paymentCommand);
        return paymentRepository.save(paymentEntity);
    }
}
