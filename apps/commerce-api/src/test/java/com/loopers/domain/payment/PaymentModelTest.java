package com.loopers.domain.payment;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PaymentModelTest {

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
            PaymentEntity paymentEntity = PaymentEntity.from(paymentCommand);

            // then
            assertEquals(paymentCommand.userId(), paymentEntity.getUserId());
            assertEquals(paymentCommand.orderId(), paymentEntity.getOrderId());
            assertEquals(paymentCommand.totalPrice(), paymentEntity.getAmount());
            assertNotNull(paymentEntity.getOrderKey());
            assertTrue(paymentEntity.getTransactions().isEmpty());
            assertEquals(PaymentEntity.State.PENDING, paymentEntity.getState());
        }
    }

    @DisplayName("update 메소드 테스트")
    @Nested
    class Update {
        @DisplayName("update 메소드가 성공 트랜잭션에 올바르게 성공 상태로 업데이트한다.")
        @Test
        void returnSuccess_whenUpdateSuccessTransaction(){
            // given
            PaymentEntity paymentEntity = new PaymentEntity(1L, 1L, "orderKey", 1000L);
            PaymentInfo.Transaction transaction = new PaymentInfo.Transaction(
                    "transactionKey", "SUCCESS", "좋아요"
            );

            // when
            paymentEntity.updateTransaction(transaction);

            // then
            assertEquals(1, paymentEntity.getTransactions().size());
            assertEquals("transactionKey", paymentEntity.getTransactions().getFirst().getTransactionKey());
            assertEquals(PaymentEntity.State.SUCCESS, paymentEntity.getState());
        }

        @DisplayName("update 메소드가 성공 트랜잭션이 포함된 리스트에 올바르게 성공 상태로 업데이트한다.")
        @Test
        void returnSuccess_whenUpdateSuccessTransactionList(){
            // given
            PaymentEntity paymentEntity = new PaymentEntity(1L, 1L, "orderKey", 1000L);
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
            paymentEntity.updateTransactions(List.of(transaction, transaction1, transaction2));

            // then
            assertEquals(3, paymentEntity.getTransactions().size());
            assertEquals(PaymentEntity.State.SUCCESS, paymentEntity.getState());
        }

        @DisplayName("update 메소드가 실패 트랜잭션에 올바르게 트랜잭션 갯수가 증가한다.")
        @Test
        void returnFail_whenUpdateFailTransaction(){
            // given
            PaymentEntity paymentEntity = new PaymentEntity(1L, 1L, "orderKey", 1000L);
            PaymentInfo.Transaction transaction = new PaymentInfo.Transaction(
                    "transactionKey", "FAIL", "좋아요"
            );

            // when
            paymentEntity.updateTransaction(transaction);

            // then
            assertEquals(1, paymentEntity.getTransactions().size());
            assertEquals("transactionKey", paymentEntity.getTransactions().getFirst().getTransactionKey());
            assertEquals(PaymentEntity.State.PENDING, paymentEntity.getState());
        }


    }

}
