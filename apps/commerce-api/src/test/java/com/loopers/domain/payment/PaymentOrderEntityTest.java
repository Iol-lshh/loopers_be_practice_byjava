package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentOrderEntityTest {

    @DisplayName("생성자 테스트")
    @Nested
    class Constructor {

        @DisplayName("생성자 호출 시 필드 값이 올바르게 설정되는지 확인")
        @org.junit.jupiter.api.Test
        void constructorSetsFieldsCorrectly() {
            // given
            PaymentCommand.RegisterOrder paymentCommand = new PaymentCommand.RegisterOrder(
                    1L, // userId
                    1L, // orderId
                    1000L // totalPrice
            );
            // when
            PaymentOrderEntity paymentOrderEntity = PaymentOrderEntity.from(paymentCommand);

            // then
            assertEquals(paymentCommand.userId(), paymentOrderEntity.getUserId());
            assertEquals(paymentCommand.orderId(), paymentOrderEntity.getOrderId());
            assertEquals(paymentCommand.totalPrice(), paymentOrderEntity.getAmount());
            assertNotNull(paymentOrderEntity.getOrderKey());
            assertTrue(paymentOrderEntity.getTransactions().isEmpty());
            assertEquals(PaymentOrderEntity.State.PENDING, paymentOrderEntity.getState());
        }
    }

    @DisplayName("update 메소드 테스트")
    @Nested
    class Update {
        @DisplayName("update 메소드가 성공 트랜잭션에 올바르게 성공 상태로 업데이트한다.")
        @Test
        void returnSuccess_whenUpdateSuccessTransaction(){
            // given
            PaymentOrderEntity paymentOrderEntity = new PaymentOrderEntity(1L, 1L, "orderKey", 1000L);
            PaymentInfo.Transaction transaction = new PaymentInfo.Transaction(
                    "transactionKey", "SUCCESS", "좋아요"
            );

            // when
            paymentOrderEntity.updateTransaction(transaction);

            // then
            assertEquals(1, paymentOrderEntity.getTransactions().size());
            assertEquals("transactionKey", paymentOrderEntity.getTransactions().getFirst().getTransactionKey());
            assertEquals(PaymentOrderEntity.State.SUCCESS, paymentOrderEntity.getState());
        }

        @DisplayName("update 메소드가 성공 트랜잭션이 포함된 리스트에 올바르게 성공 상태로 업데이트한다.")
        @Test
        void returnSuccess_whenUpdateSuccessTransactionList(){
            // given
            PaymentOrderEntity paymentOrderEntity = new PaymentOrderEntity(1L, 1L, "orderKey", 1000L);
            PaymentInfo.Transaction transaction = new PaymentInfo.Transaction(
                    "transactionKey", "FAIL", "좋아요"
            );
            PaymentInfo.Transaction transaction1 = new PaymentInfo.Transaction(
                    "transactionKey1", "SUCCESS", "좋아요"
            );
            PaymentInfo.Transaction transaction2 = new PaymentInfo.Transaction(
                    "transactionKey2", "FAIL", "정말 좋아요"
            );

            // when
            paymentOrderEntity.updateTransactions(List.of(transaction, transaction1, transaction2));

            // then
            assertEquals(3, paymentOrderEntity.getTransactions().size());
            assertEquals(PaymentOrderEntity.State.SUCCESS, paymentOrderEntity.getState());
        }

        @DisplayName("update 메소드가 실패 트랜잭션에 올바르게 트랜잭션 갯수가 증가한다.")
        @Test
        void returnFail_whenUpdateFailTransaction(){
            // given
            PaymentOrderEntity paymentOrderEntity = new PaymentOrderEntity(1L, 1L, "orderKey", 1000L);
            PaymentInfo.Transaction transaction = new PaymentInfo.Transaction(
                    "transactionKey", "FAIL", "좋아요"
            );

            // when
            paymentOrderEntity.updateTransaction(transaction);

            // then
            assertEquals(1, paymentOrderEntity.getTransactions().size());
            assertEquals("transactionKey", paymentOrderEntity.getTransactions().getFirst().getTransactionKey());
            assertEquals(PaymentOrderEntity.State.PENDING, paymentOrderEntity.getState());
        }


    }

}
