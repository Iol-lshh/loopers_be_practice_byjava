package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;

@Getter
@Embeddable
public class UserPointVo {

    @Column(nullable = false)
    private Long amount;

    protected UserPointVo() {}

    public UserPointVo(Long amount) {
        this.amount = amount;
    }

    public static UserPointVo init() {
        return new UserPointVo(0L);
    }

    public static UserPointVo plus(UserPointVo before, Long amount) {
        if (amount == null || amount == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 충전 금액은 0이거나 null일 수 없습니다: " + amount);
        }
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 충전 금액은 음수일 수 없습니다: " + amount);
        }

        return new UserPointVo(amount + before.amount);
    }
}
