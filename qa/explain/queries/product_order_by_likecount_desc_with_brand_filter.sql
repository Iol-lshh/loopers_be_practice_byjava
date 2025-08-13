-- MySQL 실행계획 확인
EXPLAIN ANALYZE
select pwse1_0.id,pwse1_0.brand_id,pwse1_0.created_at,pwse1_0.like_count,pwse1_0.name,pwse1_0.price,pwse1_0.released_at,pwse1_0.state,pwse1_0.stock,pwse1_0.updated_at from (
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
) pwse1_0 where 1=1 and pwse1_0.brand_id=1 order by pwse1_0.like_count desc
LIMIT 20 OFFSET 0;
