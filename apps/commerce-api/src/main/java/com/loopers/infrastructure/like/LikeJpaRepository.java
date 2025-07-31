package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LikeJpaRepository extends JpaRepository<LikeEntity, LikeEntity.LikeId>, JpaSpecificationExecutor<LikeEntity> {

}
