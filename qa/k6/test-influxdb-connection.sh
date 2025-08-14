#!/bin/bash

# InfluxDB 연결 테스트 스크립트
# k6가 InfluxDB에 연결할 수 있는지 확인합니다

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# InfluxDB 설정 (기본값)
INFLUXDB_URL="${INFLUXDB_URL:-http://localhost:8086}"
INFLUXDB_DB="${INFLUXDB_DB:-k6}"
INFLUXDB_USERNAME="${INFLUXDB_USERNAME:-admin}"
INFLUXDB_PASSWORD="${INFLUXDB_PASSWORD:-admin}"

echo -e "${BLUE}🔍 InfluxDB 연결 테스트 시작...${NC}"
echo -e "${BLUE}📊 InfluxDB URL: ${INFLUXDB_URL}${NC}"
echo -e "${BLUE}📊 Database: ${INFLUXDB_DB}${NC}"
echo -e "${BLUE}👤 Username: ${INFLUXDB_USERNAME}${NC}"

# 1. 포트 연결 확인
echo -e "\n${BLUE}1️⃣  포트 연결 확인...${NC}"
if command -v nc &> /dev/null; then
    if nc -z localhost 8086 2>/dev/null; then
        echo -e "${GREEN}✅ InfluxDB 포트 8086 연결 가능${NC}"
    else
        echo -e "${RED}❌ InfluxDB 포트 8086 연결 불가${NC}"
        echo -e "${YELLOW}💡 Docker 컨테이너가 실행 중인지 확인하세요:${NC}"
        echo -e "   docker-compose -f docker/monitoring-compose.yml up -d"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  netcat이 설치되어 있지 않습니다.${NC}"
fi

# 2. HTTP 연결 확인
echo -e "\n${BLUE}2️⃣  HTTP 연결 확인...${NC}"
if command -v curl &> /dev/null; then
    # InfluxDB ping 엔드포인트 확인
    PING_RESPONSE=$(curl -s -w "%{http_code}" -o /dev/null "$INFLUXDB_URL/ping" 2>/dev/null)
    
    if [ "$PING_RESPONSE" = "204" ]; then
        echo -e "${GREEN}✅ InfluxDB ping 성공 (HTTP ${PING_RESPONSE})${NC}"
    else
        echo -e "${RED}❌ InfluxDB ping 실패 (HTTP ${PING_RESPONSE})${NC}"
        exit 1
    fi
else
    echo -e "${YELLOW}⚠️  curl이 설치되어 있지 않습니다.${NC}"
fi

# 3. 데이터베이스 존재 확인 및 생성
echo -e "\n${BLUE}3️⃣  데이터베이스 확인 및 생성...${NC}"
if command -v curl &> /dev/null; then
    # 데이터베이스 목록 조회
    DB_LIST_RESPONSE=$(curl -s -u "$INFLUXDB_USERNAME:$INFLUXDB_PASSWORD" \
        "$INFLUXDB_URL/query?q=SHOW+DATABASES" 2>/dev/null)
    
    if echo "$DB_LIST_RESPONSE" | grep -q "$INFLUXDB_DB"; then
        echo -e "${GREEN}✅ 데이터베이스 '$INFLUXDB_DB' 존재함${NC}"
    else
        echo -e "${YELLOW}⚠️  데이터베이스 '$INFLUXDB_DB'가 존재하지 않습니다. 생성 중...${NC}"
        
        # 데이터베이스 생성
        CREATE_DB_RESPONSE=$(curl -s -u "$INFLUXDB_USERNAME:$INFLUXDB_PASSWORD" \
            -X POST "$INFLUXDB_URL/query" \
            --data-urlencode "q=CREATE+DATABASE+$INFLUXDB_DB" 2>/dev/null)
        
        if echo "$CREATE_DB_RESPONSE" | grep -q "results"; then
            echo -e "${GREEN}✅ 데이터베이스 '$INFLUXDB_DB' 생성 완료${NC}"
        else
            echo -e "${RED}❌ 데이터베이스 생성 실패${NC}"
            echo "응답: $CREATE_DB_RESPONSE"
            exit 1
        fi
    fi
else
    echo -e "${YELLOW}⚠️  curl이 설치되어 있지 않습니다.${NC}"
fi

# 4. k6 InfluxDB 출력 테스트
echo -e "\n${BLUE}4️⃣  k6 InfluxDB 출력 테스트...${NC}"
if command -v k6 &> /dev/null; then
    # 간단한 테스트 스크립트 생성
    TEST_SCRIPT="/tmp/k6-influxdb-test.js"
    
    cat > "$TEST_SCRIPT" << 'EOF'
import http from 'k6/http';
import { check } from 'k6';

export const options = {
  vus: 1,
  duration: '5s',
};

export default function () {
  const response = http.get('http://httpbin.org/get');
  check(response, {
    'status is 200': (r) => r.status === 200,
  });
}
EOF

    echo -e "${BLUE}📝 간단한 테스트 스크립트 생성: $TEST_SCRIPT${NC}"
    
    # k6 실행 (InfluxDB 출력만)
    echo -e "${BLUE}🚀 k6 테스트 실행 중... (5초)${NC}"
    
    # k6 InfluxDB 출력 형식: influxdb://username:password@host:port/database
    # 또는 환경 변수로 설정
    export K6_INFLUXDB_URL="$INFLUXDB_URL"
    export K6_INFLUXDB_DB="$INFLUXDB_DB"
    export K6_INFLUXDB_USERNAME="$INFLUXDB_USERNAME"
    export K6_INFLUXDB_PASSWORD="$INFLUXDB_PASSWORD"
    
    echo -e "${BLUE}🔗 InfluxDB 환경 변수 설정:${NC}"
    echo "  K6_INFLUXDB_URL: $K6_INFLUXDB_URL"
    echo "  K6_INFLUXDB_DB: $K6_INFLUXDB_DB"
    echo "  K6_INFLUXDB_USERNAME: $K6_INFLUXDB_USERNAME"
    
    k6 run "$TEST_SCRIPT" \
        --out influxdb \
        --no-usage-report \
        --quiet
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ k6 InfluxDB 출력 테스트 성공${NC}"
    else
        echo -e "${RED}❌ k6 InfluxDB 출력 테스트 실패${NC}"
        exit 1
    fi
    
    # 임시 파일 정리
    rm -f "$TEST_SCRIPT"
else
    echo -e "${RED}❌ k6가 설치되어 있지 않습니다.${NC}"
    echo -e "${YELLOW}💡 설치 방법: https://k6.io/docs/getting-started/installation/${NC}"
    exit 1
fi

# 5. 메트릭 데이터 확인
echo -e "\n${BLUE}5️⃣  메트릭 데이터 확인...${NC}"
if command -v curl &> /dev/null; then
    # 최근 메트릭 데이터 조회
    sleep 2  # 데이터 전송 대기
    
    METRICS_RESPONSE=$(curl -s -u "$INFLUXDB_USERNAME:$INFLUXDB_PASSWORD" \
        "$INFLUXDB_URL/query?db=$INFLUXDB_DB&q=SELECT+*+FROM+http_req_duration+ORDER+BY+time+DESC+LIMIT+5" 2>/dev/null)
    
    if echo "$METRICS_RESPONSE" | grep -q "results"; then
        echo -e "${GREEN}✅ 메트릭 데이터 확인 성공${NC}"
        echo -e "${BLUE}📊 최근 메트릭 데이터:${NC}"
        echo "$METRICS_RESPONSE" | jq '.results[0].series[0].values[0:3]' 2>/dev/null || echo "$METRICS_RESPONSE"
    else
        echo -e "${YELLOW}⚠️  메트릭 데이터를 찾을 수 없습니다.${NC}"
        echo "응답: $METRICS_RESPONSE"
    fi
else
    echo -e "${YELLOW}⚠️  curl이 설치되어 있지 않습니다.${NC}"
fi

echo -e "\n${GREEN}🎉 InfluxDB 연결 테스트 완료!${NC}"
echo -e "${BLUE}💡 이제 k6 테스트를 실행하면 메트릭이 InfluxDB에 저장됩니다.${NC}"
echo -e "${BLUE}📈 Grafana에서 대시보드를 확인할 수 있습니다: http://localhost:3000${NC}"
echo -e "${BLUE}🔧 환경 변수 설정: source ./set-env.sh${NC}" 