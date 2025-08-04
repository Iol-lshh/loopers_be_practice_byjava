package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "brand")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BrandEntity extends BaseEntity {
    private String name;

    public BrandEntity(String name) {
        super();
        if (name == null || name.isBlank()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        this.name = name;
    }

    public static BrandEntity of(String brandName) {
        return new BrandEntity(brandName);
    }
}
