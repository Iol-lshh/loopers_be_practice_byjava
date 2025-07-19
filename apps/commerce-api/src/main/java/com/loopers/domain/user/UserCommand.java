package com.loopers.domain.user;

public class UserCommand {
    public record Create (
            String loginId,
            UserEntity.Gender gender,
            String birthDate,
            String email
    ) {
        public static Create of (
                String loginId, String gender, String birthDate, String email
        ){
            return new Create(loginId, UserEntity.Gender.from(gender), birthDate, email);
        }
    }

}
