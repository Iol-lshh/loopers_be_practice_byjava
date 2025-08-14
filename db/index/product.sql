-- 서브쿼리 내부 필터링을 위한 인덱스
CREATE INDEX idx_product_brand_state_price ON product(brand_id, state, price);
CREATE INDEX idx_product_brand_state_released ON product(brand_id, state, released_at);

-- 단일 정렬을 위한 인덱스
CREATE INDEX idx_product_brand ON product(brand_id);
CREATE INDEX idx_product_price ON product(price);
CREATE INDEX idx_product_released ON product(released_at);
