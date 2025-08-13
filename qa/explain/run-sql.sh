#!/bin/bash

# MySQL 실행계획 분석 스크립트 (EXPLAIN ANALYZE 지원)
# 사용법: ./run-sql.sh [database_name] [host] [port] [username] [password]

# 기본값 설정
DB_NAME=${1:-"loopers"}
DB_HOST=${2:-"localhost"}
DB_PORT=${3:-"3306"}
DB_USER=${4:-"application"}
DB_PASS=${5:-"application"}

# 결과 디렉토리 생성
RESULTS_DIR="reports"
mkdir -p "$RESULTS_DIR"

# 현재 시간을 파일명에 포함
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
REPORT_FILE="$RESULTS_DIR/query_analysis_${TIMESTAMP}.md"

echo "=== MySQL 쿼리 실행계획 분석 리포트 (EXPLAIN ANALYZE) ==="
echo "생성 시간: $(date)"
echo "데이터베이스: $DB_NAME"
echo "호스트: $DB_HOST:$DB_PORT"
echo "사용자: $DB_USER"
echo "====================================="

# 마크다운 리포트 시작
echo "# MySQL 쿼리 실행계획 분석 리포트 (EXPLAIN ANALYZE)" > "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "**생성 시간**: $(date)" >> "$REPORT_FILE"
echo "**데이터베이스**: $DB_NAME" >> "$REPORT_FILE"
echo "**호스트**: $DB_HOST:$DB_PORT" >> "$REPORT_FILE"
echo "**사용자**: $DB_USER" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# MySQL 연결 테스트
echo "MySQL 연결 테스트 중..."

# MySQL 클라이언트가 있는지 확인
if command -v mysql &> /dev/null; then
    echo "MySQL 클라이언트를 사용하여 연결 테스트..."
    if ! mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" >/dev/null 2>&1; then
        echo "❌ MySQL 클라이언트로 연결 실패!"
        echo "Docker를 통해 연결을 시도합니다..."
        if ! docker exec -i docker-mysql-1 mysql -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" >/dev/null 2>&1; then
            echo "❌ Docker를 통한 연결도 실패!"
            echo "연결 정보를 확인해주세요."
            exit 1
        fi
        echo "✅ Docker를 통한 MySQL 연결 성공!"
        USE_DOCKER=true
    else
        echo "✅ MySQL 클라이언트로 연결 성공!"
        USE_DOCKER=false
    fi
elif command -v docker &> /dev/null; then
    echo "MySQL 클라이언트가 없어 Docker를 통해 연결 테스트..."
    if ! docker exec -i docker-mysql-1 mysql -u"$DB_USER" -p"$DB_PASS" -e "SELECT 1;" >/dev/null 2>&1; then
        echo "❌ Docker를 통한 MySQL 연결 실패!"
        echo "연결 정보를 확인해주세요."
    fi
    echo "✅ Docker를 통한 MySQL 연결 성공!"
    USE_DOCKER=true
else
    echo "❌ MySQL 클라이언트와 Docker 모두 사용할 수 없습니다!"
    exit 1
fi

echo "" >> "$REPORT_FILE"
echo "## 연결 정보" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "✅ **연결 상태**: 성공" >> "$REPORT_FILE"
echo "**연결 방식**: $([ "$USE_DOCKER" = true ] && echo "Docker" || echo "MySQL 클라이언트")" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"

# MySQL 명령어 실행 함수
run_mysql() {
    local sql_content="$1"
    if [ "$USE_DOCKER" = true ]; then
        docker exec -i docker-mysql-1 mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "$sql_content"
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" -e "$sql_content"
    fi
}

# MySQL 파일 실행 함수
run_mysql_file() {
    local sql_file="$1"
    if [ "$USE_DOCKER" = true ]; then
        docker exec -i docker-mysql-1 mysql -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$sql_file"
    else
        mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME" < "$sql_file"
    fi
}

# queries 디렉토리의 모든 SQL 파일 실행
QUERY_COUNT=0

for sql_file in queries/*.sql; do
    if [ -f "$sql_file" ]; then
        QUERY_COUNT=$((QUERY_COUNT + 1))
        
        echo "📋 쿼리 파일 분석: $sql_file"
        echo "========================================"
        
        echo "## 쿼리 분석: $(basename "$sql_file")" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        
        # SQL 파일 내용 출력
        echo "🔍 쿼리 내용:" | tee -a "$REPORT_FILE"
        echo '```sql' >> "$REPORT_FILE"
        cat "$sql_file" >> "$REPORT_FILE"
        echo '```' >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        
        # 실행계획 분석
        echo "📊 실행계획 분석 (EXPLAIN ANALYZE):" | tee -a "$REPORT_FILE"
        echo "----------------------------------------"
        
        # EXPLAIN ANALYZE 실행
        explain_result=$(run_mysql_file "$sql_file" 2>/dev/null)
        
        # \n 문자를 실제 줄바꿈으로 변환
        explain_result_clean=$(echo "$explain_result" | sed 's/\\n/\n/g')
        
        # 결과를 텍스트 파일로 저장
        sql_filename=$(basename "$sql_file" .sql)
        txt_filename="${RESULTS_DIR}/${sql_filename}_explain_${TIMESTAMP}.txt"
        echo "$explain_result_clean" > "$txt_filename"
        echo "💾 실행계획 텍스트 저장: $txt_filename"
        
        # 마크다운에 실행계획 추가
        echo "### 실행계획 결과" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        echo '```' >> "$REPORT_FILE"
        cat "$txt_filename" >> "$REPORT_FILE"
        echo '```' >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
        
        # 실행계획 간단 요약
        echo "📋 실행계획 요약:" | tee -a "$REPORT_FILE"
        echo "----------------------------------------"
        
        # 실행 시간 추출 (EXPLAIN ANALYZE 결과에서)
        execution_time=$(echo "$explain_result_clean" | grep -o "actual time=[0-9.]*" | head -1 | cut -d'=' -f2 | cut -d' ' -f1)
        if [ -n "$execution_time" ]; then
            echo "⏱️  실제 실행 시간: ${execution_time}ms" | tee -a "$REPORT_FILE"
        fi
        
        # 스캔된 행 수 추출
        rows_examined=$(echo "$explain_result_clean" | grep -o "rows=[0-9]*" | head -1 | cut -d'=' -f2)
        if [ -n "$rows_examined" ]; then
            echo "📊 스캔된 행 수: $rows_examined" >> "$REPORT_FILE"
        fi
        
        # 테이블 스캔 방식 추출
        access_type=$(echo "$explain_result_clean" | grep -o "type: [A-Z]*" | head -1 | cut -d' ' -f2)
        if [ -n "$access_type" ]; then
            echo "🔍 테이블 스캔 방식: $access_type" | tee -a "$REPORT_FILE"
        fi
        
        # 성능 문제점 간단 요약
        echo "" | tee -a "$REPORT_FILE"
        echo "⚠️  주요 성능 이슈:" | tee -a "$REPORT_FILE"
        
        if [ "$access_type" = "ALL" ]; then
            echo "❌ 전체 테이블 스캔 발생 (인덱스 필요)" | tee -a "$REPORT_FILE"
        fi
        
        if echo "$explain_result_clean" | grep -q "Using temporary"; then
            echo "⚠️  임시 테이블 사용 (메모리 사용량 증가)" | tee -a "$REPORT_FILE"
        fi
        
        if echo "$explain_result_clean" | grep -q "Using filesort"; then
            echo "⚠️  파일 정렬 사용 (디스크 I/O 증가)" | tee -a "$REPORT_FILE"
        fi
        
        if echo "$explain_result_clean" | grep -q "Using join buffer"; then
            echo "⚠️  조인 버퍼 사용 (메모리 사용량 증가)" | tee -a "$REPORT_FILE"
        fi
        
        echo "" >> "$REPORT_FILE"
        echo "---" >> "$REPORT_FILE"
        echo "" >> "$REPORT_FILE"
    fi
done

# 마크다운 리포트 완성
echo "" >> "$REPORT_FILE"
echo "## 요약" >> "$REPORT_FILE"
echo "" >> "$REPORT_FILE"
echo "**총 분석된 쿼리 수**: $QUERY_COUNT" >> "$REPORT_FILE"
echo "**연결 방식**: $([ "$USE_DOCKER" = true ] && echo "Docker" || echo "MySQL 클라이언트")" >> "$REPORT_FILE"
echo "**분석 방식**: EXPLAIN ANALYZE (실제 실행 시간 포함)" >> "$REPORT_FILE"
echo "**생성된 파일들**: " >> "$REPORT_FILE"
echo "- 📊 마크다운 리포트: $(basename "$REPORT_FILE")" >> "$REPORT_FILE"
echo "- 🔍 개별 실행계획 텍스트: queries 디렉토리 참조" >> "$REPORT_FILE"

echo ""
echo "=== 모든 쿼리 분석 완료 ==="
echo "✅ 분석 완료! 리포트 파일: $REPORT_FILE"

# 리포트 파일 경로 출력
echo ""
echo "📁 리포트 파일 위치: $(pwd)/$REPORT_FILE"
echo "📊 리포트 요약:"
echo "   - 총 쿼리 파일 수: $QUERY_COUNT"
echo "   - 생성된 마크다운 리포트: $REPORT_FILE"
echo "   - 리포트 크기: $(du -h "$REPORT_FILE" | cut -f1)"
echo "   - 사용된 방식: $([ "$USE_DOCKER" = true ] && echo "Docker" || echo "MySQL 클라이언트")"
echo "   - 분석 방식: EXPLAIN ANALYZE (실제 실행 시간 포함)"
