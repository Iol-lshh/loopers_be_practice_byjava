const fs = require('fs');
const path = require('path');

console.log('SQL INSERT 문 생성을 시작합니다...');

// 1. 사용자 샘플 데이터 로드 (CSV 파싱)
const userCsv = fs.readFileSync(path.join(__dirname, '../data/user_sample_100.csv'), 'utf8');
const userLines = userCsv.split('\n').slice(1); // 헤더 제거
const users = userLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => {
    const [loginId, gender, birthDate] = line.split(',');
    return {
      loginId: loginId.trim(),
      gender: gender.trim(),
      birthDate: birthDate.trim()
    };
  });

// 2. 브랜드 샘플 데이터 로드 (CSV 파싱)
const brandCsv = fs.readFileSync(path.join(__dirname, '../data/brand_sample_unique.csv'), 'utf8');
const brandLines = brandCsv.split('\n').slice(1); // 헤더 제거
const brands = brandLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

// 3. 형용사 데이터 로드 (CSV 파싱)
const adjectiveCsv = fs.readFileSync(path.join(__dirname, '../data/adjectives_1000_attributive.csv'), 'utf8');
const adjectiveLines = adjectiveCsv.split('\n').slice(1); // 헤더 제거
const adjectives = adjectiveLines
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => {
    const [predicate, attributive] = line.split(',');
    return attributive.trim(); // 형용사형 사용
  });

// 4. 명사 데이터 로드 (CSV 파싱)
const nounCsv = fs.readFileSync(path.join(__dirname, '../data/product_nouns_ko_1000.csv'), 'utf8');
const nouns = nounCsv.split('\n')
  .filter(line => line.trim()) // 빈 줄 제거
  .map(line => line.trim());

// SQL INSERT 문 생성
let sqlContent = '-- Commerce API 샘플 데이터 (100만개 상품)\n';
sqlContent += '-- 생성 시간: ' + new Date().toISOString() + '\n\n';

// 문자셋 설정 추가
sqlContent += '-- 한글 지원을 위한 문자셋 설정\n';
sqlContent += 'SET NAMES utf8mb4;\n';
sqlContent += 'SET CHARACTER SET utf8mb4;\n';
sqlContent += 'SET character_set_connection=utf8mb4;\n\n';

// 기존 데이터 정리 (외래키 제약조건 고려하여 순서 조정)
sqlContent += '-- 기존 데이터 정리\n';
sqlContent += 'SET FOREIGN_KEY_CHECKS = 0;\n';
sqlContent += 'TRUNCATE TABLE product;\n';
sqlContent += 'TRUNCATE TABLE brand;\n';
sqlContent += 'TRUNCATE TABLE member;\n';
sqlContent += 'SET FOREIGN_KEY_CHECKS = 1;\n\n';

// 사용자 INSERT
sqlContent += '-- 사용자 데이터\n';
sqlContent += 'INSERT INTO member (login_id, gender, email, birth_date, created_at, updated_at) VALUES\n';
const userValues = users.map((user, index) => {
  let sb = '';
  for(let i = 0; i < 100; i++) {
    const email = `${user.loginId + i}@example.com`;
    const safeLoginId = user.loginId.replace(/'/g, "''") + i; // SQL 이스케이프
    const safeEmail = email.replace(/'/g, "''"); // SQL 이스케이프
    sb += `('${safeLoginId}', '${user.gender}', '${safeEmail}', '${user.birthDate}', NOW(), NOW())`;
    if (i < 99) sb += ',';
  }
  return sb;
}).join(',\n');
sqlContent += userValues + ';\n\n';

// 브랜드 INSERT
sqlContent += '-- 브랜드 데이터\n';
sqlContent += 'INSERT INTO brand (name, created_at, updated_at) VALUES\n';
const brandValues = brands.map(brand => 
  `('${brand}', NOW(), NOW())`
).join(',\n');
sqlContent += brandValues + ';\n\n';

// 임시 테이블 생성 및 데이터 삽입
sqlContent += '-- 임시 테이블 생성 (형용사, 명사)\n';
sqlContent += 'CREATE TEMPORARY TABLE temp_adjectives (id INT AUTO_INCREMENT PRIMARY KEY, adjective VARCHAR(100));\n';
sqlContent += 'CREATE TEMPORARY TABLE temp_nouns (id INT AUTO_INCREMENT PRIMARY KEY, noun VARCHAR(100));\n\n';

// 형용사 임시 테이블에 삽입
sqlContent += '-- 형용사 데이터 삽입\n';
sqlContent += 'INSERT INTO temp_adjectives (adjective) VALUES\n';
const adjectiveValues = adjectives.map(adj => 
  `('${adj.replace(/'/g, "''")}')`
).join(',\n');
sqlContent += adjectiveValues + ';\n\n';

// 명사 임시 테이블에 삽입
sqlContent += '-- 명사 데이터 삽입\n';
sqlContent += 'INSERT INTO temp_nouns (noun) VALUES\n';
const nounValues = nouns.map(noun => 
  `('${noun.replace(/'/g, "''")}')`
).join(',\n');
sqlContent += nounValues + ';\n\n';

// 크로스 조인으로 100만개 상품 생성
sqlContent += '-- 크로스 조인으로 100만개 상품 생성\n';
sqlContent += 'INSERT INTO product (name, price, stock, brand_id, state, released_at, created_at, updated_at)\n';
sqlContent += 'SELECT \n';
sqlContent += '  CONCAT(a.adjective, \' \', n.noun) as name,\n';
sqlContent += '  (a.id * 1000 + n.id * 100 + FLOOR(RAND() * 10000)) as price,\n';
sqlContent += '  GREATEST(1, FLOOR(RAND() * 100)) as stock,\n';
sqlContent += '  (FLOOR(RAND() * ' + brands.length + ') + 1) as brand_id,\n';
sqlContent += '  CASE WHEN FLOOR(RAND() * 100) > 20 THEN \'OPEN\' ELSE \'OUT_OF_STOCK\' END as state,\n';
sqlContent += '  DATE_ADD(\'2020-01-01\', INTERVAL FLOOR(RAND() * 1460) DAY) as released_at,\n';
sqlContent += '  NOW() as created_at,\n';
sqlContent += '  NOW() as updated_at\n';
sqlContent += 'FROM temp_adjectives a\n';
sqlContent += 'CROSS JOIN temp_nouns n\n';
sqlContent += 'LIMIT 1000000;\n\n';

// 임시 테이블 정리
sqlContent += '-- 임시 테이블 정리\n';
sqlContent += 'DROP TEMPORARY TABLE IF EXISTS temp_adjectives;\n';
sqlContent += 'DROP TEMPORARY TABLE IF EXISTS temp_nouns;\n';

// SQL 파일 저장
fs.writeFileSync(path.join(__dirname, '../results/sample-data.sql'), sqlContent);

console.log('SQL 파일이 생성되었습니다: ../results/sample-data.sql');
console.log(`- 사용자: ${users.length}명`);
console.log(`- 브랜드: ${brands.length}개`);
console.log(`- 상품: 1,000,000개 (크로스 조인으로 생성)`);
console.log('\n이제 다음 명령으로 데이터베이스에 삽입할 수 있습니다:');
console.log('./insert-sample-data.sh'); 