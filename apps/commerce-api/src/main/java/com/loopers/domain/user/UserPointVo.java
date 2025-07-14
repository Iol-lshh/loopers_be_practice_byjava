package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Embeddable
@Table(name = "point")
public class UserPointVo {

    private Long amount;

    protected UserPointVo() {}

    public UserPointVo(Long amount) {
        this.amount = amount;
    }

    public static UserPointVo init() {
        return new UserPointVo(0L);
    }

    public UserPointVo charge(Long amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 0원 이하가 될 수 없습니다: " + amount);
        }
        var newOne = init();
        newOne.amount = this.amount + amount;
        return newOne;
    }
}
