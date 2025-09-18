-- 테스트용 제품 메트릭 데이터 삽입
-- baseDate가 2024-01-07인 경우, 2024-01-01 ~ 2024-01-07까지의 데이터

-- 제품 1번 데이터 (7일간)
INSERT INTO product_metrics (product_id, metric_name, metric_value, metric_date) VALUES
(1, 'view_count', '100', '2024-01-01'),
(1, 'like_count', '10', '2024-01-01'),
(1, 'sold_count', '5', '2024-01-01'),
(1, 'sold_amount', '50000', '2024-01-01'),

(1, 'view_count', '120', '2024-01-02'),
(1, 'like_count', '15', '2024-01-02'),
(1, 'sold_count', '7', '2024-01-02'),
(1, 'sold_amount', '70000', '2024-01-02'),

(1, 'view_count', '90', '2024-01-03'),
(1, 'like_count', '8', '2024-01-03'),
(1, 'sold_count', '3', '2024-01-03'),
(1, 'sold_amount', '30000', '2024-01-03'),

(1, 'view_count', '110', '2024-01-04'),
(1, 'like_count', '12', '2024-01-04'),
(1, 'sold_count', '6', '2024-01-04'),
(1, 'sold_amount', '60000', '2024-01-04'),

(1, 'view_count', '130', '2024-01-05'),
(1, 'like_count', '18', '2024-01-05'),
(1, 'sold_count', '8', '2024-01-05'),
(1, 'sold_amount', '80000', '2024-01-05'),

(1, 'view_count', '95', '2024-01-06'),
(1, 'like_count', '9', '2024-01-06'),
(1, 'sold_count', '4', '2024-01-06'),
(1, 'sold_amount', '40000', '2024-01-06'),

(1, 'view_count', '105', '2024-01-07'),
(1, 'like_count', '11', '2024-01-07'),
(1, 'sold_count', '5', '2024-01-07'),
(1, 'sold_amount', '50000', '2024-01-07');

-- 제품 2번 데이터 (7일간)
INSERT INTO product_metrics (product_id, metric_name, metric_value, metric_date) VALUES
(2, 'view_count', '200', '2024-01-01'),
(2, 'like_count', '20', '2024-01-01'),
(2, 'sold_count', '10', '2024-01-01'),
(2, 'sold_amount', '100000', '2024-01-01'),

(2, 'view_count', '180', '2024-01-02'),
(2, 'like_count', '16', '2024-01-02'),
(2, 'sold_count', '8', '2024-01-02'),
(2, 'sold_amount', '80000', '2024-01-02'),

(2, 'view_count', '220', '2024-01-03'),
(2, 'like_count', '25', '2024-01-03'),
(2, 'sold_count', '12', '2024-01-03'),
(2, 'sold_amount', '120000', '2024-01-03'),

(2, 'view_count', '210', '2024-01-04'),
(2, 'like_count', '22', '2024-01-04'),
(2, 'sold_count', '11', '2024-01-04'),
(2, 'sold_amount', '110000', '2024-01-04'),

(2, 'view_count', '190', '2024-01-05'),
(2, 'like_count', '18', '2024-01-05'),
(2, 'sold_count', '9', '2024-01-05'),
(2, 'sold_amount', '90000', '2024-01-05'),

(2, 'view_count', '240', '2024-01-06'),
(2, 'like_count', '28', '2024-01-06'),
(2, 'sold_count', '14', '2024-01-06'),
(2, 'sold_amount', '140000', '2024-01-06'),

(2, 'view_count', '230', '2024-01-07'),
(2, 'like_count', '26', '2024-01-07'),
(2, 'sold_count', '13', '2024-01-07'),
(2, 'sold_amount', '130000', '2024-01-07');

-- 제품 3번 데이터 (일부 날짜만)
INSERT INTO product_metrics (product_id, metric_name, metric_value, metric_date) VALUES
(3, 'view_count', '50', '2024-01-05'),
(3, 'like_count', '5', '2024-01-05'),
(3, 'sold_count', '2', '2024-01-05'),
(3, 'sold_amount', '20000', '2024-01-05'),

(3, 'view_count', '60', '2024-01-06'),
(3, 'like_count', '6', '2024-01-06'),
(3, 'sold_count', '3', '2024-01-06'),
(3, 'sold_amount', '30000', '2024-01-06'),

(3, 'view_count', '55', '2024-01-07'),
(3, 'like_count', '7', '2024-01-07'),
(3, 'sold_count', '2', '2024-01-07'),
(3, 'sold_amount', '25000', '2024-01-07');
