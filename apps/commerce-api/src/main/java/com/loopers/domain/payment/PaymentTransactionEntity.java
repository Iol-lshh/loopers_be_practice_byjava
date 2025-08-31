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

@Getter
@Entity
@Table(name = "payment_transaction")
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class PaymentTransactionEntity extends BaseEntity {
    private String transactionKey;
    private String status;
    private String reason;
    private Long amount;
    private Type type;

    @ManyToOne
    @JoinColumn(name = "payment_id")
    private PaymentEntity paymentOrder;

    public PaymentTransactionEntity(PaymentEntity paymentOrder, String transactionKey, String status, String reason) {
        if(transactionKey == null || transactionKey.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Transact key cannot be null or blank");
        }
        if(status == null || status.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Status cannot be null or blank");
        }
        this.paymentOrder = paymentOrder;
        this.transactionKey = transactionKey;
        this.status = status;
        this.reason = reason;
    }

    public static PaymentTransactionEntity from(PaymentEntity order, PaymentCommand.UpdateTransaction transaction) {
        return new PaymentTransactionEntity(
                order,
                transaction.transactionKey(),
                transaction.status(),
                transaction.reason()
        );
    }

    public PaymentTransactionEntity update(PaymentCommand.UpdateTransaction transaction) {
        if (transaction == null) {
            throw new CoreException(ErrorType.BAD_REQUEST, "Transact cannot be null");
        }
        this.status = transaction.status();
        this.reason = transaction.reason();
        return this;
    }

    public enum Type {
        POINT,
        PG
    }
}
