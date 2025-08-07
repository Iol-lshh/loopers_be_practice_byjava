package com.loopers.domain.like;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Getter
@Entity
@Table(name = "likes")
@IdClass(LikeEntity.LikeId.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeEntity {
    @Id
    private Long userId;
    
    @Id
    private Long targetId;
    
    @Id
    @Enumerated(EnumType.STRING)
    private TargetType targetType;

    public LikeEntity(Long userId, Long targetId, TargetType targetType) {
        super();
        if (userId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (targetId == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        if (targetType == null) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.userId = userId;
        this.targetId = targetId;
        this.targetType = targetType;
    }

    public static LikeEntity from(LikeCommand.Product command) {
        return new LikeEntity(
                command.userId(),
                command.targetId(),
                TargetType.PRODUCT
        );
    }

    public enum TargetType {
        PRODUCT
    } 

    @NoArgsConstructor(access = AccessLevel.PROTECTED)
    @EqualsAndHashCode
    @Embeddable
    public static class LikeId implements Serializable {
        private Long userId;
        private Long targetId;
        private TargetType targetType;
    }
}
