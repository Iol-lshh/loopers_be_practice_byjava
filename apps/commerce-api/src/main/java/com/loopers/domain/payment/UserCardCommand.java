package com.loopers.domain.payment;

public class UserCardCommand {
    public record Register(
            Long userId,
            String cardNumber,
            String cardType
    ) {

        public UserCardEntity toEntity() {
            return new UserCardEntity(
                    userId,
                    cardNumber,
                    cardType
            );
        }
    }
}
