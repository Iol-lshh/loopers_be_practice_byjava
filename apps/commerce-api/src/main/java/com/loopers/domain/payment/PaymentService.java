package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
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
        log.info("PaymentService.request 시작 - userId: {}, orderId: {}", requestCommand.userId(), requestCommand.orderId());
        
        UserCardEntity cardEntity = userCardRepository.findByUserId(requestCommand.userId()).orElseThrow(() -> new CoreException(
                ErrorType.BAD_REQUEST, "사용자의 카드 정보를 찾을 수 없습니다. userId: " + requestCommand.userId())
        );
        log.info("사용자 카드 정보 조회 성공 - cardType: {}, cardNumber: {}", cardEntity.getCardType(), cardEntity.getCardNumber());
        
        PaymentEntity payment = paymentRepository.findByOrderId(requestCommand.orderId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "결제 주문을 찾을 수 없습니다. orderId: " + requestCommand.orderId())
        );
        log.info("결제 주문 정보 조회 성공 - orderKey: {}, amount: {}", payment.getOrderKey(), payment.getAmount());

        PaymentStatement.Request requestStatement = new PaymentStatement.Request(
                payment.getOrderKey(),
                payment.getUserId(),
                payment.getAmount(),
                cardEntity.getCardNumber(),
                cardEntity.getCardType()
        );
        log.info("PG 결제 요청 준비 완료 - orderKey: {}, userId: {}, amount: {}, cardType: {}", 
                requestStatement.orderKey(), requestStatement.userId(), requestStatement.totalPrice(), requestStatement.cardType());
        
        try{
            log.info("PG 결제 요청 시작");
            PaymentInfo.Transaction result = paymentGateway.request(requestStatement);
            log.info("PG 결제 요청 성공 - transactionKey: {}, status: {}, reason: {}", 
                    result.transactionKey(), result.status(), result.reason());
            
            PaymentCommand.UpdateTransaction updateCommand = PaymentCommand.UpdateTransaction
                    .from(result, requestCommand.userId(), payment.getId());
            log.info("UpdateTransaction 이벤트 발행 - paymentId: {}, transactionKey: {}", 
                    updateCommand.paymentId(), updateCommand.transactionKey());
            
            eventPublisher.publishEvent(updateCommand);
            log.info("UpdateTransaction 이벤트 발행 완료");
        } catch (Exception e) {
            PaymentCommand.Cancel cancelCommand = new PaymentCommand.Cancel(requestCommand.orderId());
            eventPublisher.publishEvent(cancelCommand);
            log.error("PG 결제 요청 실패", e);
            log.error("예외 상세: {}", e.toString());
            log.error("예외 메시지: {}", e.getMessage());
        }
    }

    public UserCardEntity registerCard(UserCardCommand.Register command) {
        UserCardEntity userCardEntity = command.toEntity();
        return userCardRepository.save(userCardEntity);
    }

    public PaymentEntity register(PaymentCommand.RegisterOrder paymentCommand) {
        PaymentEntity paymentEntity = PaymentEntity.from(paymentCommand);
        return paymentRepository.save(paymentEntity);
    }

    @Transactional
    public PaymentEntity update(PaymentCommand.UpdateTransaction command) {
        PaymentEntity payment = paymentRepository.findById(command.paymentId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "결제 정보를 찾을 수 없습니다. paymentId: " + command.paymentId()));
        payment.updateTransaction(command);
        return paymentRepository.save(payment);
    }

    public Optional<PaymentEntity> findByOrderKey(String orderKey) {
        return paymentRepository.findByOrderKey(orderKey);
    }
}
