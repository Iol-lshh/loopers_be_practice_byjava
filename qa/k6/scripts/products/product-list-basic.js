import http from 'k6/http';
import { check, sleep } from 'k6';

// 상품 목록 기본 조회 테스트 설정
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

const REQUEST_DURATION = 2000; // 동시 요청 고려하여 임계값 증가
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

// 상품 목록 기본 조회 테스트 시나리오
export default function () {
  // 시나리오 1: 기본 상품 목록 조회 (최신순)
  const basicResponse = http.get(`${BASE_URL}/api/v1/products?sort=latest`);
  
  // 응답 시간 상세 로깅
  console.log(`[VU: ${__VU}] Response time - Total: ${basicResponse.timings.duration}ms, Waiting: ${basicResponse.timings.waiting}ms, Connecting: ${basicResponse.timings.connecting}ms`);
  
  check(basicResponse, {
    '기본 상품 목록 조회 성공': (r) => r.status === 200,
    '기본 상품 목록 응답 시간 체크': (r) => r.timings.duration < REQUEST_DURATION,
    '기본 상품 목록 응답 크기 체크': (r) => r.body && r.body.length > 0,
  });

  // 요청 간격을 줄여서 동시 요청 효과 확인
  sleep(1);
} 