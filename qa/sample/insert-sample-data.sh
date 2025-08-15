#!/bin/bash

# 샘플 데이터 삽입 스크립트
echo "=== Commerce API 샘플 데이터 삽입 ==="

# 환경 변수 설정
DB_HOST=${DB_HOST:-"localhost"}
DB_PORT=${DB_PORT:-"3306"}
DB_NAME=${DB_NAME:-"loopers"}
DB_USER=${DB_USER:-"application"}
DB_PASSWORD=${DB_PASSWORD:-"application"}

echo "데이터베이스: $DB_HOST:$DB_PORT/$DB_NAME"
echo "사용자: $DB_USER"

# 1. SQL 파일 생성
echo ""
echo "1단계: SQL 파일 생성"
node scripts/generate-sql.js

# 2. SQL 파일 확인
echo ""
echo "2단계: 생성된 SQL 파일 확인"
ls -la results/

# 3. 데이터베이스에 삽입
echo ""
echo "3단계: 데이터베이스에 샘플 데이터 삽입"
if command -v mysql &> /dev/null; then
    echo "MySQL 클라이언트를 사용하여 데이터 삽입..."
    mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME < results/sample-data.sql
    echo "데이터 삽입 완료!"
elif command -v docker &> /dev/null; then
    echo "Docker를 사용하여 데이터 삽입..."
    # MySQL 컨테이너 찾기
    MYSQL_CONTAINER=$(docker ps --filter "ancestor=mysql:8.0" --format "{{.Names}}" | head -1)
    if [ -n "$MYSQL_CONTAINER" ]; then
        echo "MySQL 컨테이너 발견: $MYSQL_CONTAINER"
        docker exec -i $MYSQL_CONTAINER mysql -u $DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME < results/sample-data.sql
        echo "데이터 삽입 완료!"
    else
        echo "MySQL 컨테이너를 찾을 수 없습니다."
        echo "수동으로 다음 명령을 실행하세요:"
        echo "docker exec -i [컨테이너명] mysql -u $DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME < results/sample-data.sql"
    fi
else
    echo "MySQL 클라이언트나 Docker가 설치되지 않았습니다."
    echo "수동으로 다음 명령을 실행하세요:"
    echo "mysql -h $DB_HOST -P $DB_PORT -u $DB_USER -p$DB_PASSWORD --default-character-set=utf8mb4 $DB_NAME < results/sample-data.sql"
fi

echo ""
echo "=== 샘플 데이터 삽입 완료 ==="
echo "이제 성능 테스트를 실행할 수 있습니다: ./run-tests.sh" 