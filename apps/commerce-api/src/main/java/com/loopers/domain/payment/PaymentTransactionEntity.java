package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Entity
@Table(name = "payment_transaction")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentTransactionEntity extends BaseEntity {
    private String transactionKey;
    private String status;
    private String reason;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentOrderEntity paymentOrder;

    public PaymentTransactionEntity(PaymentOrderEntity paymentOrder, String transactionKey, String status, String reason) {
        if(transactionKey == null || transactionKey.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Transaction key cannot be null or blank");
        }
        if(status == null || status.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Status cannot be null or blank");
        }
        this.paymentOrder = paymentOrder;
        this.transactionKey = transactionKey;
        this.status = status;
        this.reason = reason;
    }

    public static List<PaymentTransactionEntity> of(PaymentOrderEntity order, List<PaymentInfo.Transaction> transactions) {
        return transactions.stream()
                .map(transaction -> from(order, transaction))
                .toList();
    }

    public static PaymentTransactionEntity from(PaymentOrderEntity order, PaymentInfo.Transaction transaction) {
        return new PaymentTransactionEntity(
                order,
                transaction.transactionKey(),
                transaction.status(),
                transaction.reason()
        );
    }

    public PaymentTransactionEntity update(PaymentInfo.Transaction transaction) {
        if (transaction == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Transaction cannot be null");
        }
        this.status = transaction.status();
        this.reason = transaction.reason();
        return this;
    }
}
