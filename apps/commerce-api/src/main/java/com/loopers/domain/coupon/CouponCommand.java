package com.loopers.domain.coupon;

import java.util.List;

public class CouponCommand {
    public static class Admin {
        public record Create(
                String type,
                Long value
        ){
            public CouponEntity toEntity() {
                CouponEntity.Type couponType = CouponEntity.Type.from(type);
                return new CouponEntity(couponType, value);
            }
        }
    }

    public static class User {
        public record Order(
                Long userId,
                List<Long> couponIds
        ){
        }

    }
}

