package com.loopers.application.payment;

import com.loopers.domain.payment.PaymentCommand;
import com.loopers.domain.payment.PaymentEntity;
import com.loopers.domain.payment.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class PaymentEventHandler {

    private final PaymentService paymentService;

    @EventListener
    public void handle(PaymentCommand.UpdateTransaction command) {
        log.info("PaymentEventHandler.handle 시작 - paymentId: {}, transactionKey: {}, status: {}",
                command.paymentId(), command.transactionKey(), command.status());
        
        try {
            PaymentEntity result = paymentService.update(command);
            log.info("PaymentTransaction 업데이트 성공 - paymentId: {}, transactionCount: {}", 
                    result.getId(), result.getTransactions().size());
        } catch (Exception e) {
            log.error("PaymentTransaction 업데이트 실패", e);
            throw e;
        }
    }
}
