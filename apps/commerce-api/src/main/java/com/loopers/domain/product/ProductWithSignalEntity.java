package com.loopers.domain.product;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Subselect;
import org.springframework.data.annotation.Immutable;

import java.time.LocalDateTime;

@Entity
@Subselect("""
    SELECT 
        p.id,
        p.name,
        p.brand_id,
        p.price,
        p.stock,
        p.created_at,
        p.updated_at,
        p.state,
        p.released_at,
        
        COALESCE(ls.like_count, 0) as like_count
    FROM product p
    LEFT JOIN like_summary ls ON p.id = ls.target_id AND ls.target_type = 'PRODUCT'
    """)
@Immutable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductWithSignalEntity {
    @Id
    private Long id;
    
    private String name;
    private Long brandId;
    private Long price;
    private Long stock;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "value", column = @Column(name = "state")),
        @AttributeOverride(name = "releasedAt", column = @Column(name = "released_at"))
    })
    private ProductEntity.State state;
    
    private Long likeCount;
} 
