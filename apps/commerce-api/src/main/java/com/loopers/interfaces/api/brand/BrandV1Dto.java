package com.loopers.interfaces.api.brand;

import com.loopers.application.brand.BrandResult;

public class BrandV1Dto {
    public record Response(
            Long id, String name
    ) {
        public static Response from(BrandResult info) {
            return new Response(
                info.id(),
                info.name()
            );
        }
    }
}
