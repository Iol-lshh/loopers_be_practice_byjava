package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductWithSignal;
import com.loopers.domain.like.LikeSummaryEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductJpaRepository extends JpaRepository<ProductEntity, Long>, JpaSpecificationExecutor<ProductEntity> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM ProductEntity p WHERE p.id IN :ids")
    List<ProductEntity> lockAllById(List<Long> ids);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.id = :id""")
    Optional<ProductWithSignal> findWithSignal(Long id);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.id IN :ids""")
    List<ProductWithSignal> findAllWithSignal(List<Long> ids);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
ORDER BY ls.likeCount DESC NULLS LAST, p.id ASC
""")
    List<ProductWithSignal> findAllWithSignalOrderByLikeCountDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
ORDER BY ls.likeCount DESC NULLS LAST, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalByBrandIdOrderByLikeCountDesc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
ORDER BY p.price ASC, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalOrderByPriceAsc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
ORDER BY p.price ASC, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalByBrandIdOrderByPriceAsc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
ORDER BY p.price DESC, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalOrderByPriceDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
ORDER BY p.price DESC, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalByBrandIdOrderByPriceDesc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
ORDER BY p.state.releasedAt DESC NULLS LAST, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalOrderByReleasedAtDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
ORDER BY p.state.releasedAt DESC NULLS LAST, p.id ASC""")
    List<ProductWithSignal> findAllWithSignalByBrandIdOrderByReleasedAtDesc(Long brandId, Pageable pageable);

}
