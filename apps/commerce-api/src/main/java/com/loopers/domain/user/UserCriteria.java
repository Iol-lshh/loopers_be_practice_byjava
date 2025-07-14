package com.loopers.domain.user;

import lombok.Getter;

@Getter
public class UserCriteria {
    private String loginId;

    public static UserCriteria byLoginId(String loginId) {
        UserCriteria criteria = new UserCriteria();
        criteria.loginId = loginId;
        return criteria;
    }
}
