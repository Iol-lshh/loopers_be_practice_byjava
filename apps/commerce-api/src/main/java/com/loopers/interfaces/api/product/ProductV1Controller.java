package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.domain.product.ProductStatement;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductV1Controller {

    private final ProductFacade productFacade;

    @GetMapping("/{productId}")
    public ApiResponse<ProductV1Dto.DetailResponse> get(@PathVariable Long productId) {
        var result = productFacade.get(productId);
        return ApiResponse.success(ProductV1Dto.DetailResponse.from(result));
    }

    @GetMapping("")
    public ApiResponse<List<ProductV1Dto.SummaryResponse>> list(
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false, defaultValue = "latest") String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size
    ) {
        var cirteriaBuilder = ProductStatement.builder();
        if(brandId != null) {
            cirteriaBuilder.brandID(brandId);
        }
        if(sort != null) {
            switch (sort) {
                case "latest" -> cirteriaBuilder.orderBy(new ProductStatement.ReleasedAt(false));
                case "price_asc" -> cirteriaBuilder.orderBy(new ProductStatement.Price(true));
                case "likes_desc" -> cirteriaBuilder.orderBy(new ProductStatement.LikeCount());
                default -> throw new IllegalArgumentException("지원하지 않는 정렬 기준입니다: " + sort);
            }
        }
        var criteria = cirteriaBuilder.build();
        var pageable = PageRequest.of(page, size);
        var result = productFacade.list(criteria, pageable);
        return ApiResponse.success(ProductV1Dto.SummaryResponse.of(result));
    }
}
