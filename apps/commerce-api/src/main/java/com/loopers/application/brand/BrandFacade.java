package com.loopers.application.brand;

import com.loopers.domain.brand.BrandEntity;
import com.loopers.domain.brand.BrandService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class BrandFacade {

    private final BrandService brandService;

    @Transactional(readOnly = true)
    public BrandResult get(Long brandId) {

        BrandEntity brand = brandService.find(brandId).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다 : " + brandId)
        );
        return new BrandResult(
                brand.getId(),
                brand.getName()
        );
    }
}
