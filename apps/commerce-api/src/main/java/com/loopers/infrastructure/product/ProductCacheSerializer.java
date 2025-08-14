package com.loopers.infrastructure.product;

import java.util.List;

public class ProductCacheSerializer {
    public static String serializeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (Long id : ids) {
            if (!sb.isEmpty()) {
                sb.append(",");
            }
            sb.append(id);
        }
        return sb.toString();
    }
}
