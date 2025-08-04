package com.loopers.interfaces.api.like;

import com.loopers.application.like.LikeFacade;
import com.loopers.application.product.ProductFacade;
import com.loopers.domain.product.ProductStatement;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/like")
public class LikeV1Controller {

    private final LikeFacade likeFacade;
    private final ProductFacade productFacade;

    @PostMapping("/products/{productId}")
    public ApiResponse<LikeV1Dto.Response> likeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId) {
        var result = likeFacade.likeProduct(userId, productId);
        return ApiResponse.success(LikeV1Dto.Response.from(result));
    }

    @DeleteMapping("/products/{productId}")
    public ApiResponse<LikeV1Dto.Response> unlikeProduct(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long productId) {
        var result = likeFacade.unlikeProduct(userId, productId);
        return ApiResponse.success(LikeV1Dto.Response.from(result));
    }

    @GetMapping("/products")
    public ApiResponse<List<LikeV1Dto.ProductResponse>> listLikes(
            @RequestHeader("X-USER-ID") Long userId) {
        ProductStatement statement = ProductStatement.builder()
                .userId(userId)
                .build();
        var result = productFacade.list(userId);
        return ApiResponse.success(LikeV1Dto.ProductResponse.from(result));
    }
}
