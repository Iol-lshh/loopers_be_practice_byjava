package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;
    private final UserCardRepository userCardRepository;
    private final ApplicationEventPublisher eventPublisher;

    public Optional<PaymentEntity> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId);
    }

    @Transactional
    public PaymentEntity transact(PaymentCommand.Transact command) {
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
                ErrorType.NOT_FOUND, "결제 주문을 찾을 수 없습니다. orderId: " + requestCommand.orderId())
        );
        PaymentStatement.Request requestStatement = new PaymentStatement.Request(
                payment.getOrderKey(),
                payment.getUserId(),
                payment.getAmount(),
                cardEntity.getCardNumber(),
                cardEntity.getCardType()
        );
        try{
            PaymentInfo.Transaction result = paymentGateway.request(requestStatement);
            PaymentEvent.Pg.Pending pendingEvent = new PaymentEvent.Pg.Pending(
                    result,
                    requestCommand.userId(),
                    requestCommand.orderId(),
                    payment.getId()
            );
            eventPublisher.publishEvent(pendingEvent);
        } catch (CoreException e) {
            log.error("PG 결제 요청 실패", e);
            log.error("예외 상세: {}", e.toString());
            log.error("예외 메시지: {}", e.getMessage());
            PaymentEvent.Failed failedEvent = new PaymentEvent.Failed(requestCommand.orderId());
            eventPublisher.publishEvent(failedEvent);
        }
    }

    @Transactional
    public UserCardEntity registerCard(UserCardCommand.Register command) {
        UserCardEntity userCardEntity = command.toEntity();
        return userCardRepository.save(userCardEntity);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentEntity register(PaymentCommand.RegisterOrder paymentCommand) {
        PaymentEntity paymentEntity = PaymentEntity.from(paymentCommand);
        return paymentRepository.save(paymentEntity);
    }

    @Transactional
    public PaymentEntity update(PaymentCommand.UpdateTransaction command) {
        PaymentEntity payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다. paymentId: " + command.paymentId()));
        payment.updateTransaction(command);
        if(payment.getState().equals(PaymentEntity.State.SUCCESS)){
            PaymentEvent.Success event = PaymentEvent.Success.from(payment);
            eventPublisher.publishEvent(event);
        }
        return paymentRepository.save(payment);
    }

    @Transactional(readOnly = true)
    public Optional<PaymentEntity> findByOrderKey(String orderKey) {
        return paymentRepository.findByOrderKey(orderKey);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentEntity updateState(PaymentCommand.Fail failCommand) {
        PaymentEntity payment = paymentRepository.findByOrderId(failCommand.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 주문을 찾을 수 없습니다. orderId: " + failCommand.orderId()
        ));
        payment.fail();
        PaymentEvent.Failed failedEvent = new PaymentEvent.Failed(failCommand.orderId());
        eventPublisher.publishEvent(failedEvent);
        return paymentRepository.save(payment);
    }
}
