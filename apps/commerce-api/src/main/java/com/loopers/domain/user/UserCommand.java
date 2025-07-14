package com.loopers.domain.user;

public class UserCommand {
    public record Create(
            String loginId,
            Gender gender,
            String birthDate,
            String email
    ){
        public static Create of (
                String loginId, String gender, String birthDate, String email
        ){
            return new Create(loginId, Gender.from(gender), birthDate, email);
        }
    }

}
