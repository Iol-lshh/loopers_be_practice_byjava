package com.loopers.domain.coupon;

import java.util.List;

public class CouponCommand {
    public static class Admin {
        public record Create(
                String type,
                Long value
        ){
            public CouponEntity toEntity() {
                CouponEntity.Type couponType = CouponEntity.Type.valueOf(type.toUpperCase());
                return new CouponEntity(couponType, value);
            }
        }
    }

    public static class User {
        public record Order(
                List<Long> couponIds
        ){
        }

    }
}

