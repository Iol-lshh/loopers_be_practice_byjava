-- MySQL 실행계획 확인
EXPLAIN ANALYZE
select pe1_0.id,pe1_0.brand_id,pe1_0.created_at,pe1_0.deleted_at,pe1_0.name,pe1_0.price,pe1_0.released_at,pe1_0.state,pe1_0.stock,pe1_0.updated_at,lse1_0.like_count
from product pe1_0
left join like_summary lse1_0 on pe1_0.id=lse1_0.target_id and lse1_0.target_type='PRODUCT'
where pe1_0.brand_id=1
order by lse1_0.like_count desc,pe1_0.id
limit 0 offset 20;
