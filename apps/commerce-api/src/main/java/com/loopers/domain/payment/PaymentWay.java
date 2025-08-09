package com.loopers.domain.payment;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

public interface PaymentWay<R> {
    R pay(Long userId, Long totalPrice);
    Type getType();

    @Getter
    enum Type {
        POINT("POINT")
        ;

        private final String value;

        Type(String value) {
            this.value = value;
        }

        public static Type of(String type) {
            for (Type t : Type.values()) {
                if (t.value.equalsIgnoreCase(type)) {
                    return t;
                }
            }
            throw new CoreException(ErrorType.BAD_REQUEST, "존재하지 않는 타입: " + type);
        }
    }
}
