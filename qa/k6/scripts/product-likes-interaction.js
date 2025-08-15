import http from 'k6/http';
import { check, sleep } from 'k6';

// 랜덤 정수 생성 함수 (k6/utils 대신 직접 구현)
function randomIntBetween(min, max) {
  return Math.floor(Math.random() * (max - min + 1)) + min;
}

// 상품 좋아요 상호작용 테스트 설정 (10000개 상품 대상)
export const options = {
  vus: 1000,       // 1000명의 사용자 (동시 테스트)
  duration: '1m', // 1분 실행
  thresholds: {
    http_req_duration: ['p(95)<500'], // 95% 요청이 500ms 이내
    http_req_failed: ['rate<0.05'],   // 에러율 5% 미만
    checks: ['rate>0.95'],            // 체크 성공률 95% 이상
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

const REQUEST_DURATION = 500;
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const MAX_PRODUCTS = 1000; // 최대 상품 수

// 상품 좋아요 상호작용 테스트 시나리오 (10000개 상품 자유 테스트)
export default function () {
  // 사용자 ID는 VU 번호를 기반으로 생성 (1부터 시작)
  const userId = __VU;

  const productIds = [];
  for (let i = 1; i <= MAX_PRODUCTS; i++) {
    productIds.push(i);
  }

  // 시나리오 1: 다양한 상품에 좋아요 테스트
  for (let i = 0; i < productIds.length; i++) {
    const productId = productIds[i];
    const likeResponse = http.post(`${BASE_URL}/api/v1/like/products/${productId}`, null, {
      headers: {
        'X-USER-ID': userId.toString(),
        'Content-Type': 'application/json',
      },
    });
    
    check(likeResponse, {
      [`상품 ${productId} 좋아요 성공`]: (r) => r.status === 200,
      [`상품 ${productId} 좋아요 응답 시간 체크`]: (r) => r.timings.duration < REQUEST_DURATION,
      [`상품 ${productId} 좋아요 응답 크기 체크`]: (r) => r.body.length > 0,
    });

    sleep(0.5);
  }

  sleep(1);
} 