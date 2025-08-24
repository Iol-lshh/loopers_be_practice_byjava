package com.loopers.util.uuid;

import java.util.UUID;
import java.time.Instant;

public class UuidV7Generator {
    public static UUID generateUuidV7() {
        // 1. 현재 타임스탬프 (epoch milliseconds) 가져오기
        long epochMillis = Instant.now().toEpochMilli();

        // 2. 무작위 값 생성
        // SecureRandom을 사용하는 것이 더 안전하지만, 예제에서는 Random을 사용
        long randomA = (long) (Math.random() * (1L << 12)); // 12비트 랜덤
        long randomB = (long) (Math.random() * (1L << 62)); // 62비트 랜덤

        // 3. 비트 마스킹 및 쉬프트 연산으로 UUID 구조 맞추기

        // 'most significant bits' (MSB) - 상위 64비트
        // 타임스탬프(48비트) + 버전(4비트) + 랜덤A(12비트)
        long msb = 0L;
        msb |= (epochMillis & 0xFFFFFFFFFFFFL) << 16;
        msb |= 0x7000; // 버전 7 (0111)
        msb |= randomA;

        // 'least significant bits' (LSB) - 하위 64비트
        // 변형(2비트) + 랜덤B(62비트)
        long lsb = 0L;
        lsb |= 0x8000000000000000L; // 변형 (10xx)
        lsb |= randomB;

        return new UUID(msb, lsb);
    }
}
