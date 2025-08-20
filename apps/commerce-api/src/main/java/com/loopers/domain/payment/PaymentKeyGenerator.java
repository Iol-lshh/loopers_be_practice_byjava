package com.loopers.domain.payment;

import com.loopers.util.uuid.UuidV7Generator;

import java.util.UUID;

public class PaymentKeyGenerator {
    public static String generateOrderKey() {
        UUID uuid = UuidV7Generator.generateUuidV7();
        return uuid.toString();
    }
}
