CREATE INDEX idx_ls_type_like_tid
    ON like_summary (target_type, like_count DESC, target_id);
CREATE INDEX idx_ls_type_tid
    ON like_summary (target_type, target_id);


-- 브랜드 없이 목록 뽑을 때
CREATE INDEX idx_product_state_price_id
    ON product (state, price, id);

-- 브랜드 필터 자주 쓰면 추가 (정렬까지 인덱스로 해결)
CREATE INDEX idx_product_state_brand_price_id
    ON product (state, brand_id, price, id);

-- (지금 없음) 출시일 케이스에서 꼭 필요
CREATE INDEX idx_product_state_rel_id
    ON product (state, released_at DESC, id);

-- 브랜드 필터 자주 쓰면 추가
CREATE INDEX idx_product_state_brand_rel_id
    ON product (state, brand_id, released_at DESC, id);

-- 필터 전용(가벼운 스캔)
CREATE INDEX idx_product_state_brand_id
    ON product (state, brand_id, id);

--
CREATE INDEX idx_product_state_id
    ON product (state, id);