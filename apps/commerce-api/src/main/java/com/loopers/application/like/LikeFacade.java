package com.loopers.application.like;

import com.loopers.domain.like.*;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final UserService userService;
    private final ProductService productService;

    @Transactional
    public LikeResult.Result likeProduct(Long userId, Long productId) {
        userService.find(userId).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다 : " + userId));
        productService.find(productId).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다 : " + productId));

        var command = new LikeCommand.Product(userId, productId);
        LikeEntity like = likeService.register(command);

        return LikeResult.Result.of(like, true);
    }

    @Transactional
    public LikeResult.Result unlikeProduct(Long userId, Long productId) {
        userService.find(userId).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다 : " + userId));
        productService.find(productId).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "상품을 찾을 수 없습니다 : " + productId));

        LikeCommand.Product command = new LikeCommand.Product(userId, productId);
        LikeEntity like = likeService.remove(command);

        return LikeResult.Result.of(like, false);
    }
}
