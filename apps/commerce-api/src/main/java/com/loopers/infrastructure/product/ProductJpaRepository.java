package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductEntity;
import com.loopers.domain.product.ProductInfo;
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
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.id = :id
""")
    Optional<ProductInfo.ProductWithSignal> findWithSignal(Long id);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.id IN :ids""")
    List<ProductInfo.ProductWithSignal> findAllWithSignal(List<Long> ids);

    @Query(nativeQuery = true, value = """
WITH
    hot AS (
        SELECT ls.target_id, ls.like_count
        FROM like_summary ls FORCE INDEX (idx_ls_type_like_tid)
                 JOIN product p FORCE INDEX (idx_product_state_brand_id)
                      ON p.id = ls.target_id
        WHERE ls.target_type = 'PRODUCT'
          AND p.state = 'OPEN'
        ORDER BY ls.like_count DESC, ls.target_id
        LIMIT 100
    ),
    cold AS (
        SELECT p.id
        FROM product p FORCE INDEX (idx_product_state_brand_id)
                 LEFT JOIN like_summary ls
                           ON ls.target_id = p.id AND ls.target_type = 'PRODUCT'
        WHERE p.state = 'OPEN'
          AND ls.target_id IS NULL
        ORDER BY p.id
        LIMIT 100
    )
SELECT
    u.id, p.brand_id, p.created_at, p.name, p.price,
    p.released_at, p.state, p.stock, p.updated_at, u.like_count
    FROM (SELECT 0 b, h.target_id id, h.like_count FROM hot h
    UNION ALL
    SELECT 1 b, c.id, NULL FROM cold c) u
    JOIN product p ON p.id = u.id
ORDER BY u.b, u.like_count DESC, p.id
""")
    List<ProductWithSignalRow> findAllWithSignalOrderByLikeCountDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
AND p.state.value = 'OPEN'
ORDER BY ls.likeCount DESC NULLS LAST, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalByBrandIdOrderByLikeCountDesc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.state.value = 'OPEN'
ORDER BY p.price ASC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalOrderByPriceAsc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
AND p.state.value = 'OPEN'
ORDER BY p.price ASC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalByBrandIdOrderByPriceAsc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.state.value = 'OPEN'
ORDER BY p.price DESC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalOrderByPriceDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
AND p.state.value = 'OPEN'
ORDER BY p.price DESC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalByBrandIdOrderByPriceDesc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.state.value = 'OPEN'
ORDER BY p.state.releasedAt DESC NULLS LAST, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalOrderByReleasedAtDesc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
AND p.state.value = 'OPEN'
ORDER BY p.state.releasedAt DESC NULLS LAST, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalByBrandIdOrderByReleasedAtDesc(Long brandId, Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.state.value = 'OPEN'
ORDER BY p.state.releasedAt ASC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalOrderByReleasedAtAsc(Pageable pageable);

    @Query("""
SELECT new com.loopers.domain.product.ProductInfo$ProductWithSignal(p, ls.likeCount)
FROM ProductEntity p
LEFT JOIN LikeSummaryEntity ls ON p.id = ls.targetId AND ls.targetType = 'PRODUCT'
WHERE p.brandId = :brandId
AND p.state.value = 'OPEN'
ORDER BY p.state.releasedAt ASC, p.id ASC""")
    List<ProductInfo.ProductWithSignal> findAllWithSignalByBrandIdOrderByReleasedAtAsc(Long brandId, Pageable pageable);
}
