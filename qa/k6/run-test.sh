#!/bin/bash

# k6 성능 테스트 실행 및 결과 요약 스크립트
# 실행 전에 k6가 설치되어 있는지 확인

# 색상 정의
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 현재 시간을 파일명에 사용할 타임스탬프
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORTS_DIR="reports"
SCRIPTS_DIR="scripts"

# InfluxDB 설정
INFLUXDB_URL="${INFLUXDB_URL:-http://localhost:8086}"
INFLUXDB_DB="${INFLUXDB_DB:-k6}"
INFLUXDB_USERNAME="${INFLUXDB_USERNAME:-admin}"
INFLUXDB_PASSWORD="${INFLUXDB_PASSWORD:-admin}"

echo -e "${BLUE}🚀 k6 성능 테스트 시작...${NC}"
echo -e "${BLUE}📅 실행 시간: ${TIMESTAMP}${NC}"
echo -e "${BLUE}📊 InfluxDB URL: ${INFLUXDB_URL}${NC}"
echo -e "${BLUE}📊 InfluxDB Database: ${INFLUXDB_DB}${NC}"

# k6 설치 확인
if ! command -v k6 &> /dev/null; then
    echo -e "${RED}❌ k6가 설치되어 있지 않습니다.${NC}"
    echo -e "${YELLOW}📖 설치 방법: https://k6.io/docs/getting-started/installation/${NC}"
    exit 1
fi

# 네트워크 진단
echo -e "${BLUE}🔍 네트워크 진단 중...${NC}"

# Docker 컨테이너 상태 확인
if command -v docker &> /dev/null; then
    echo -e "${BLUE}🐳 Docker 컨테이너 상태 확인:${NC}"
    docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
    
    # 네트워크 확인
    echo -e "${BLUE}🌐 Docker 네트워크 확인:${NC}"
    docker network ls
    docker network inspect docker_default 2>/dev/null | grep -E "(Name|Driver|Subnet|Gateway)" || echo -e "${YELLOW}⚠️  docker_default 네트워크를 찾을 수 없습니다.${NC}"
else
    echo -e "${YELLOW}⚠️  Docker가 설치되어 있지 않습니다.${NC}"
fi

# 포트 연결 확인
echo -e "${BLUE}🔌 포트 연결 확인:${NC}"
if command -v nc &> /dev/null; then
    if nc -z localhost 8080 2>/dev/null; then
        echo -e "${GREEN}✅ 포트 8080 연결 가능${NC}"
    else
        echo -e "${RED}❌ 포트 8080 연결 불가${NC}"
    fi
    
    # InfluxDB 포트 확인
    if nc -z localhost 8086 2>/dev/null; then
        echo -e "${GREEN}✅ InfluxDB 포트 8086 연결 가능${NC}"
    else
        echo -e "${RED}❌ InfluxDB 포트 8086 연결 불가${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  netcat이 설치되어 있지 않습니다.${NC}"
fi

# reports 디렉토리 생성
mkdir -p "$REPORTS_DIR"

echo -e "${BLUE}📁 테스트 스크립트 실행 중...${NC}"

# 1단계: products 폴더의 모든 k6 스크립트 실행
echo -e "${BLUE}📦 products 폴더의 k6 스크립트들 실행 중...${NC}"

if [ -d "$SCRIPTS_DIR/products" ]; then
    for script in "$SCRIPTS_DIR/products"/*.js; do
        if [ -f "$script" ]; then
            script_name=$(basename "$script" .js)
            echo -e "${GREEN}✅ $script_name 실행 중...${NC}"
            
            # 네트워크 진단을 위한 환경 변수 설정
            export BASE_URL="http://localhost:8080"
            echo -e "${BLUE}🌐 BASE_URL: $BASE_URL${NC}"
            
            # k6 실행 시 InfluxDB와 JSON 출력 모두 설정
            # InfluxDB 환경 변수 설정
            export K6_INFLUXDB_URL="$INFLUXDB_URL"
            export K6_INFLUXDB_DB="$INFLUXDB_DB"
            export K6_INFLUXDB_USERNAME="$INFLUXDB_USERNAME"
            export K6_INFLUXDB_PASSWORD="$INFLUXDB_PASSWORD"
            
            k6 run "$script" \
                --out json="$REPORTS_DIR/$script_name-$TIMESTAMP.json" \
                --out influxdb
            
            if [ $? -eq 0 ]; then
                echo -e "${GREEN}✅ $script_name 실행 완료 (InfluxDB + JSON 출력)${NC}"
            else
                echo -e "${RED}❌ $script_name 실행 실패${NC}"
            fi
        fi
    done
else
    echo -e "${YELLOW}⚠️  products 폴더를 찾을 수 없습니다.${NC}"
fi

# 2단계: 결과 요약 MD 파일 생성
echo -e "${BLUE}📝 결과 요약 보고서 생성 중...${NC}"

if [ -f "$SCRIPTS_DIR/report-writer.js" ]; then
    echo -e "${GREEN}✅ report-writer.js 실행 중...${NC}"
    node "$SCRIPTS_DIR/report-writer.js" "$TIMESTAMP" "$REPORTS_DIR"
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 결과 요약 보고서 생성 완료${NC}"
    else
        echo -e "${RED}❌ 결과 요약 보고서 생성 실패${NC}"
    fi
else
    echo -e "${YELLOW}⚠️  report-writer.js 파일을 찾을 수 없습니다. 수동으로 요약 보고서를 생성합니다.${NC}"
    
    # 수동으로 간단한 요약 보고서 생성
    SUMMARY_FILE="$REPORTS_DIR/performance_summary_$TIMESTAMP.md"
    
    cat > "$SUMMARY_FILE" << EOF
# k6 성능 테스트 결과 요약

**실행 시간:** $TIMESTAMP

## 실행된 테스트 스크립트

EOF

    # JSON 결과 파일들을 찾아서 요약 정보 추가
    for json_file in "$REPORTS_DIR"/*"$TIMESTAMP".json; do
        if [ -f "$json_file" ]; then
            echo "### $(basename "$json_file" .json)" >> "$SUMMARY_FILE"
            echo "파일 경로: \`$json_file\`" >> "$SUMMARY_FILE"
            echo "" >> "$SUMMARY_FILE"
        fi
    done

    echo -e "${GREEN}✅ 수동 요약 보고서 생성 완료: $SUMMARY_FILE${NC}"
fi

echo -e "${BLUE}🎉 모든 테스트 실행 및 결과 요약 완료!${NC}"
echo -e "${BLUE}📁 결과 파일 위치: $REPORTS_DIR/${NC}"
echo -e "${BLUE}📅 타임스탬프: $TIMESTAMP${NC}"
echo -e "${BLUE}📊 InfluxDB 메트릭 저장 완료: $INFLUXDB_URL (DB: $INFLUXDB_DB)${NC}"
echo -e "${BLUE}📈 Grafana 대시보드: http://localhost:3000 (admin/admin)${NC}"
