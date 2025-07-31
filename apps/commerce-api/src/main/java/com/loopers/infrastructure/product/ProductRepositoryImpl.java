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
    private final ProductWithSignalJpaRepository withSignalJpaRepository;

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

    // ProductWithSignal 메서드 구현
    @Override
    public Optional<ProductWithSignalEntity> findWithSignal(Long id) {
        return withSignalJpaRepository.findById(id);
    }

    @Override
    public List<ProductWithSignalEntity> findWithSignals(ProductStatement criteria, Pageable pageable) {
        var spec = ProductJpaSpecification.withSignalFrom(criteria);
        return withSignalJpaRepository.findAll(spec, pageable).getContent();
    }

    @Override
    public List<ProductWithSignalEntity> findWithSignals(List<Long> ids) {
        return withSignalJpaRepository.findAllById(ids);
    }

}
