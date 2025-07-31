package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "point")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointEntity extends BaseEntity {

    @Column(nullable = false)
    private Long userId;

    private Long amount;

    public PointEntity (Long userId, long amount) {
        super();
        this.userId = userId;
        this.amount = amount;
    }

    public static PointEntity init(Long userId) {
        return new PointEntity(userId, 0L);
    }

    public PointEntity add(long amount) {
        if (amount == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 충전 금액은 0일 수 없습니다: " + amount);
        }
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 충전 금액은 음수일 수 없습니다: " + amount);
        }
        if (this.amount > Long.MAX_VALUE - amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "최대 충전 포인트를 초과합니다.: " + this.amount + " + "+ amount);
        }
        this.amount += amount;
        return this;
    }

    public PointEntity subtract(long amount) {
        if (amount == 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 차감 금액은 0일 수 없습니다: " + amount);
        }
        if (amount < 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트 차감 금액은 음수일 수 없습니다: " + amount);
        }
        if (this.amount < amount) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트가 부족합니다: 현재 포인트 = " + this.amount + ", 차감할 포인트 = " + amount);
        }
        this.amount -= amount;
        return this;
    }
}
