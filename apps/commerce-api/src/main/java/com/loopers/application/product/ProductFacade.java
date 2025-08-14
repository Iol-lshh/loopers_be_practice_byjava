package com.loopers.application.product;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeEntity;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeStatement;
import com.loopers.domain.like.LikeSummaryEntity;
import com.loopers.domain.product.*;
import com.loopers.domain.user.UserService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class ProductFacade {

    private final ProductService productService;
    private final BrandService brandService;
    private final LikeService likeService;
    private final UserService userService;
    private final ProductCacheRepository productCacheRepository;

    @Transactional(readOnly = true)
    public List<ProductResult.Summary> list(Pageable pageable) {

        ProductStatement criteria = ProductStatement.builder()
                .orderBy(new ProductStatement.CreatedAt(false))
                .build();
        return list(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductResult.Summary> list(ProductStatement criteria, Pageable pageable) {
        List<Long> cacheKeys = productCacheRepository.findIds(criteria, pageable);

        List<ProductWithSignal> productWithSignals = !cacheKeys.isEmpty() ?
                productService.findWithSignals(cacheKeys) :
                productService.findWithSignals(criteria, pageable);

        if (cacheKeys.isEmpty()) {
            productCacheRepository.save(criteria, pageable, productWithSignals);
        }

        List<Long> brandIds = productWithSignals.stream().map(ProductWithSignal::getBrandId).distinct().toList();
        List<BrandEntity> brands = brandService.find(brandIds);

        return ProductResult.Summary.of(brands, productWithSignals);
    }

    @Transactional(readOnly = true)
    public ProductResult.Detail get(Long id) {
        ProductWithSignal productWithSignal = productService.findWithSignal(id).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "조회할 수 없는 상품입니다: " + id));
        BrandEntity brand = brandService.find(productWithSignal.getBrandId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "조회할 수 없는 브랜드입니다: " + productWithSignal.getBrandId()));

        return ProductResult.Detail.of(brand, productWithSignal);
    }

    @Transactional
    public ProductResult.Detail release(Long id) {
        ProductEntity releasedProduct = productService.release(id);
        BrandEntity brand = brandService.find(releasedProduct.getBrandId()).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "조회할 수 없는 브랜드입니다: " + releasedProduct.getBrandId()));
        LikeSummaryEntity likeSummary = likeService.findSummary(releasedProduct.getId(), LikeEntity.TargetType.PRODUCT)
                .orElseGet(() -> LikeSummaryEntity.of(releasedProduct.getId(), LikeEntity.TargetType.PRODUCT));

        return ProductResult.Detail.of(brand, releasedProduct, likeSummary);
    }

    @Transactional(readOnly = true)
    public List<ProductResult.Summary> list(Long userId) {
        userService.find(userId).orElseThrow(()-> new CoreException(
                ErrorType.NOT_FOUND, "사용자를 찾을 수 없습니다 : " + userId));
        LikeStatement likeStatement = LikeStatement.builder()
                .userId(userId).build();
        List<LikeEntity> likeList = likeService.find(likeStatement);

        var productIds = likeList.stream().map(LikeEntity::getTargetId).toList();
        List<ProductWithSignal> productWithSignals = productService.findWithSignals(productIds);

        var brandIds = productWithSignals.stream().map(ProductWithSignal::getBrandId).distinct().toList();
        List<BrandEntity> brandList = brandService.find(brandIds);

        return ProductResult.Summary.of(brandList, productWithSignals);
    }
}
