package com.loopers.domain.payment;

import com.loopers.domain.point.PointEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

public interface PaymentWay<R> {
    R pay(Long userId, Long totalPrice);
    Type getType();

    @Getter
    enum Type {
        POINT("POINT", PointEntity.class)
        ;

        private final String value;
        private final Class<?> classType;

        Type(String value, Class<?> pointEntityClass) {
            this.value = value;
            this.classType = pointEntityClass;
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
