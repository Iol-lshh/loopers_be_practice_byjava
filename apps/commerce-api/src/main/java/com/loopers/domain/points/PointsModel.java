package com.loopers.domain.points;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "points")
public class PointsModel extends BaseEntity {

    private Long userId;
    private Long amount;

    protected PointsModel() {}

    public PointsModel(Long userId, Long amount) {
        super();
        this.userId = userId;
        this.amount = amount;
    }

    public static PointsModel from(Long userId) {
        return new PointsModel(userId, 0L);
    }

    public PointsModel charge(Long amount) {
        if (amount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "포인트는 0원 이하가 될 수 없습니다: " + amount);
        }
        this.amount += amount;
        return this;
    }
}
