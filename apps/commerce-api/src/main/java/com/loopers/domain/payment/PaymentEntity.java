package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
@Entity
@Table(name = "payment_order")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentEntity extends BaseEntity {
    private Long orderId;
    private Long userId;
    private String orderKey;
    private Long amount;
    private State state;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "paymentOrder")
    private List<PaymentTransactionEntity> transactions;

    public PaymentEntity(Long orderId, Long userId, String orderKey, Long amount) {
        this.orderId = orderId;
        this.userId = userId;
        this.orderKey = orderKey;
        this.amount = amount;
        this.state = State.PENDING;
        this.transactions = new ArrayList<>();
    }

    public static PaymentEntity from(PaymentCommand.RegisterOrder paymentCommand) {
        String orderKey = PaymentKeyGenerator.generateOrderKey();
        return new PaymentEntity(
                paymentCommand.orderId(), paymentCommand.userId(), orderKey, paymentCommand.totalPrice()
        );
    }

    public PaymentEntity update(PaymentInfo.Order orderInfo) {
        updateTransactions(orderInfo.transactions());
        return this;
    }

    public PaymentEntity updateTransaction(PaymentInfo.Transaction transaction) {
        Optional<PaymentTransactionEntity> existingTransaction = this.transactions.stream()
                .filter(t -> t.getTransactionKey().equals(transaction.transactionKey()))
                .findFirst();

        if (existingTransaction.isPresent()) {
            existingTransaction.get().update(transaction);
        } else {
            PaymentTransactionEntity newTransaction = PaymentTransactionEntity.from(this, transaction);
            this.transactions.add(newTransaction);
        }
        if(transaction.status().equals(State.SUCCESS.name())) {
            this.state = State.SUCCESS;
        }
        return this;
    }

    public enum State {
        PENDING,
        SUCCESS,
        FAILED
    }

    public PaymentEntity updateTransactions(List<PaymentInfo.Transaction> transactions) {
        transactions.forEach(this::updateTransaction);
        return this;
    }
}
