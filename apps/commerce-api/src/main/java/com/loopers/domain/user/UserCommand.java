package com.loopers.domain.user;

public interface UserCommand {
    record Create (
            String loginId,
            Gender gender,
            String birthDate,
            String email
    ) implements UserCommand {
        public static Create of (
                String loginId, String gender, String birthDate, String email
        ){
            return new Create(loginId, Gender.from(gender), birthDate, email);
        }
    }

}
