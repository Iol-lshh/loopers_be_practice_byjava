package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.Getter;

@Getter
public enum Gender {
    MALE("남"),
    FEMALE("여"),
    ;

    private final String value;

    Gender(String value) {
        this.value = value;
    }

    public static Gender from(String value) {
        if (value == null || value.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 비어있을 수 없습니다: " + value);
        }
        
        return switch (value) {
            case "남" -> MALE;
            case "여" -> FEMALE;
            default -> throw new CoreException(ErrorType.BAD_REQUEST, "지원하지 않는 성별입니다: " + value);
        };
    }
}
