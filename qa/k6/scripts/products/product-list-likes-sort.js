import http from 'k6/http';
import { check, sleep } from 'k6';

// 상품 목록 좋아요순 정렬 테스트 설정
export const options = {
  stages: [
    { duration: '10s', target: 1 },
    { duration: '10s', target: 10 },
    { duration: '10s', target: 100 },
    { duration: '10s', target: 0 },
  ],
  thresholds: {
    http_req_duration: ['p(95)<300'],
    http_req_failed: ['rate<0.05'],    // 에러율 5% 미만
    checks: ['rate>0.95'],             // 체크 성공률 95% 이상
  },
  // InfluxDB 출력 설정
  ext: {
    loadimpact: {
      distribution: {
        'influxdb': { loadZone: 'amazon:us:ashburn', percent: 100 }
      }
    }
  }
};

const REQUEST_DURATION = 300;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 상품 목록 좋아요순 정렬 테스트 시나리오
export default function () {
  // 시나리오 1: 좋아요 내림차순 정렬 (기본)
  const likesDescResponse = http.get(`${BASE_URL}/api/v1/products?sort=likes_desc`);
  check(likesDescResponse, {
    '좋아요 내림차순 정렬 조회 성공': (r) => r.status === 200,
    '좋아요 내림차순 응답 시간 체크': (r) => r.timings.duration < REQUEST_DURATION,
    '좋아요 내림차순 응답 크기 체크': (r) => r.body.length > 0,
  });

  sleep(1);
} 