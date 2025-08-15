-- MySQL 실행계획 확인
EXPLAIN ANALYZE
WITH
    hot AS (
        SELECT ls.target_id, ls.like_count
        FROM like_summary ls FORCE INDEX (idx_ls_type_like_tid)
                 JOIN product p FORCE INDEX (idx_product_state_brand_id)
                      ON p.id = ls.target_id
        WHERE ls.target_type = 'PRODUCT'
          AND p.state = 'OPEN'
        ORDER BY ls.like_count DESC
        LIMIT 20
    ),
    cold AS (
        SELECT p.id
        FROM product p FORCE INDEX (idx_product_state_brand_id)
        LEFT JOIN like_summary ls ON ls.target_id = p.id AND ls.target_type = 'PRODUCT'
        WHERE p.state = 'OPEN'
          AND ls.target_id IS NULL
        ORDER BY p.released_at DESC
        LIMIT 20
    )
SELECT
    u.id, p.brand_id, p.created_at, p.deleted_at, p.name, p.price,
    p.released_at, p.state, p.stock, p.updated_at, u.like_count
FROM (
    SELECT 0 b, h.target_id id, h.like_count FROM hot h
    UNION ALL
    SELECT 1 b, c.id, 0 FROM cold c
) u
JOIN product p ON p.id = u.id
ORDER BY u.b, u.like_count DESC, p.id
    LIMIT 20;




EXPLAIN ANALYZE
select
    pe1_0.id,pe1_0.brand_id,pe1_0.created_at,pe1_0.deleted_at,pe1_0.name,pe1_0.price,
    pe1_0.released_at,pe1_0.state,pe1_0.stock,pe1_0.updated_at,lse1_0.like_count
from product pe1_0
         left join like_summary lse1_0 on pe1_0.id=lse1_0.target_id and lse1_0.target_type='PRODUCT'
where pe1_0.state = 'OPEN'
order by lse1_0.like_count desc,pe1_0.id
limit 20 offset 0;


