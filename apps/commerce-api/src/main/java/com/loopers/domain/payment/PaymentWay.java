package com.loopers.domain.payment;

public interface PaymentWay<R> {
    R pay(Long userId, Long totalPrice);
    Type getType();

    enum Type {
        POINT("POINT"),
        CREDIT("CREDIT"),;

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
            throw new IllegalArgumentException("존재하지 않는 타입: " + type);
        }
    }
}
