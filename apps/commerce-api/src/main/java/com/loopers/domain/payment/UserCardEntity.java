package com.loopers.domain.payment;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserCardEntity extends BaseEntity {
    private Long userId;
    private String cardType;
    private String cardNumber;

    public UserCardEntity(
            Long userId,
            String cardType,
            String cardNumber
    ) {
        this.userId = userId;
        this.cardType = cardType;
        this.cardNumber = cardNumber;
    }

    public enum CardType {
        SAMSUNG,
        KB,
        HYUNDAI
        ;
    }
}
