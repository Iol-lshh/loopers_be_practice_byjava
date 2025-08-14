package com.loopers.domain.product;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductReader productReader;

    @Transactional
    public ProductEntity register(ProductCommand.Register command) {
        ProductEntity product = ProductEntity.from(command);
        return productRepository.save(product);
    }

    @Transactional(readOnly = true)
    public Optional<ProductEntity> find(Long id) {
        return productRepository.find(id);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> find(List<Long> ids) {
        return productRepository.findList(ids);
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> assertDeductable(Map<Long, Long> orderQuantityList) {
        List<ProductEntity> products = productRepository.findList(orderQuantityList.keySet().stream().toList());
        if (products.size() != orderQuantityList.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문할 상품이 존재하지 않습니다.");
        }
        for (ProductEntity product : products) {
            Long quantity = orderQuantityList.get(product.getId());
            if (product.getStock() < quantity) {
                throw new CoreException(ErrorType.BAD_REQUEST, "재고가 부족합니다: " + product.getId());
            }
        }
        return products;
    }

    @Transactional
    public List<ProductEntity> deduct(Map<Long, Long> orderQuantityList) {
        var targetIds = orderQuantityList.keySet().stream().toList();
        List<ProductEntity> products= productRepository.findListWithLock(targetIds);
        if( products.size() != orderQuantityList.size()) {
            throw new CoreException(ErrorType.NOT_FOUND, "주문할 상품이 존재하지 않습니다.");
        }
        for (ProductEntity product : products) {
            product.deductStock(orderQuantityList.get(product.getId()));
            productRepository.save(product);
        }
        return products;
    }

    // ProductWithSignal 메서드들
    @Transactional(readOnly = true)
    public Optional<ProductWithSignal> findWithSignal(Long id) {
        return productReader.findWithSignal(id);
    }

    @Transactional(readOnly = true)
    public List<ProductWithSignal> findWithSignals(ProductStatement criteria, Pageable pageable) {
        return productReader.findWithSignals(criteria, pageable);
    }

    @Transactional(readOnly = true)
    public List<ProductWithSignal> findWithSignals(List<Long> ids) {
        return productReader.findWithSignals(ids);
    }

    @Transactional
    public ProductEntity release(Long id) {
        ProductEntity product = productRepository.find(id).orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, "조회할 수 없는 상품입니다: " + id));
        product.release();
        return productRepository.save(product);
    }
}
