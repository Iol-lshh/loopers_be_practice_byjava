package com.loopers.infrastructure.product;

import com.loopers.domain.product.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository, ProductReader {

    private final ProductJpaRepository productJpaRepository;

    @Override
    public ProductEntity save(ProductEntity product) {
        return productJpaRepository.save(product);
    }

    @Override
    public Optional<ProductEntity> find(Long id) {
        return productJpaRepository.findById(id);
    }

    @Override
    public List<ProductEntity> findList(ProductStatement criteria, Pageable pageable) {
        var spec = ProductJpaSpecification.from(criteria);
        return productJpaRepository.findAll(spec, pageable).getContent();
    }

    @Override
    public List<ProductEntity> findList(List<Long> ids) {
        return productJpaRepository.findAllById(ids);
    }

    @Override
    public List<ProductEntity> findListWithLock(List<Long> ids) {
        return productJpaRepository.lockAllById(ids);
    }

    // ProductWithSignal 메서드 구현
    @Override
    public Optional<ProductWithSignal> findWithSignal(Long id) {
        return productJpaRepository.findWithSignal(id);
    }

    @Override
    public List<ProductWithSignal> findWithSignals(ProductStatement statement, Pageable pageable) {
        if(statement.getBrandId() != null) {
            if (statement.getOrderBy() instanceof ProductStatement.LikeCount) {
                return productJpaRepository.findAllWithSignalByBrandIdOrderByLikeCountDesc(statement.getBrandId(), pageable);
            } else if (statement.getOrderBy() instanceof ProductStatement.Price(boolean ascending)) {
                return ascending ? productJpaRepository.findAllWithSignalByBrandIdOrderByPriceAsc(statement.getBrandId(), pageable)
                        : productJpaRepository.findAllWithSignalByBrandIdOrderByPriceDesc(statement.getBrandId(), pageable);
            } else if (statement.getOrderBy() instanceof ProductStatement.ReleasedAt) {
                return productJpaRepository.findAllWithSignalByBrandIdOrderByReleasedAtDesc(statement.getBrandId(), pageable);
            } else {
                // 기본 정렬 (ReleasedAt DESC)
                return productJpaRepository.findAllWithSignalByBrandIdOrderByReleasedAtDesc(statement.getBrandId(), pageable);
            }
        }

        if (statement.getOrderBy() instanceof ProductStatement.LikeCount) {
            return productJpaRepository.findAllWithSignalOrderByLikeCountDesc(pageable);
        } else if (statement.getOrderBy() instanceof ProductStatement.Price(boolean ascending)) {
            return ascending ? productJpaRepository.findAllWithSignalOrderByPriceAsc(pageable)
                    : productJpaRepository.findAllWithSignalOrderByPriceDesc(pageable);
        } else if (statement.getOrderBy() instanceof ProductStatement.ReleasedAt) {
            return productJpaRepository.findAllWithSignalOrderByReleasedAtDesc(pageable);
        } else {
            // 기본 정렬 (ReleasedAt DESC)
            return productJpaRepository.findAllWithSignalOrderByReleasedAtDesc(pageable);
        }
    }

    @Override
    public List<ProductWithSignal> findWithSignals(List<Long> ids) {
        return productJpaRepository.findAllWithSignal(ids);
    }

}
