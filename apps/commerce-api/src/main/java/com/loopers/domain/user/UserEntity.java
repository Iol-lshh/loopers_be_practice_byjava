package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
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
}
