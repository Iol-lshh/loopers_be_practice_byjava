package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.loopers.domain.user.UserValidator.*;

@Getter
@Slf4j
@Entity
@Table(name = "member")
public class UserEntity extends BaseEntity {

    private String loginId;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false, length = 3)
    private Gender gender;

    private String birthDate;

    private String email;

    @Embedded
    private UserPointVo point;

    protected UserEntity() {}

    public UserEntity(String loginId, Gender gender, String birthDate, String email) {
        super();
        validateLoginId(loginId);
        validateBirthDate(birthDate);
        validateEmail(email);
        this.loginId = loginId;
        this.gender = gender;
        this.birthDate = birthDate;
        this.email = email;
        this.point = UserPointVo.init();
    }

    public static UserEntity of(UserCommand.Create command){
        return new UserEntity(command.loginId(), command.gender(), command.birthDate(), command.email());
    }

    public UserPointVo charge(Long amount) {
        this.point = this.point.charge(amount);
        return this.point;
    }

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
}
