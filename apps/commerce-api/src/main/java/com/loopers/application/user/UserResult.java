package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;

public record UserResult(
        Long id,
        String loginId,
        String gender,
        String birthDate,
        String email
) {
    public static UserResult from(UserEntity model) {
        return new UserResult(
                model.getId(),
                model.getLoginId(),
                model.getGender().getValue(),
                model.getBirthDate(),
                model.getEmail()
        );
    }
}
