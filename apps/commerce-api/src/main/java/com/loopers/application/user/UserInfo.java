package com.loopers.application.user;

import com.loopers.domain.user.UserEntity;

public record UserInfo(
        Long id,
        String loginId,
        String gender,
        String birthDate,
        String email
) {
    public static UserInfo from(UserEntity model) {
        return new UserInfo(
                model.getId(),
                model.getLoginId(),
                model.getGender().getValue(),
                model.getBirthDate(),
                model.getEmail()
        );
    }
}
