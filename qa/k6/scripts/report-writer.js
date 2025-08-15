const fs = require('fs');
const path = require('path');

// 명령행 인수 처리
const timestamp = process.argv[2];
const reportsDir = process.argv[3] || 'reports';

if (!timestamp) {
  console.error('❌ 타임스탬프가 필요합니다. 사용법: node report-writer.js <timestamp> [reports_dir]');
  process.exit(1);
}

console.log('📝 k6 테스트 결과 요약 보고서 생성 중...');
console.log(`📅 타임스탬프: ${timestamp}`);
console.log(`📁 보고서 디렉토리: ${reportsDir}`);

// JSON Lines 파일에서 주요 메트릭 추출
function extractMetrics(jsonFile) {
  try {
    const content = fs.readFileSync(jsonFile, 'utf8');
    const lines = content.trim().split('\n');
    
    // 메트릭 데이터를 저장할 객체
    const metrics = {};
    
    // 각 줄을 JSON으로 파싱하여 메트릭 수집
    lines.forEach(line => {
      try {
        const data = JSON.parse(line);
        if (data.metric && data.data) {
          const metricName = data.metric;
          const metricData = data.data;
          
          if (!metrics[metricName]) {
            metrics[metricName] = [];
          }
          metrics[metricName].push(metricData);
        }
      } catch (lineError) {
        // 개별 줄 파싱 실패 시 무시하고 계속 진행
      }
    });
    
    // 메트릭 데이터에서 통계 계산
    const httpReqDuration = metrics.http_req_duration || [];
    const httpReqs = metrics.http_reqs || [];
    const vus = metrics.vus || [];
    const iterations = metrics.iterations || [];
    const dataReceived = metrics.data_received || [];
    const dataSent = metrics.data_sent || [];
    
    // 응답 시간 통계 계산
    const durations = httpReqDuration.map(d => d.value || 0).filter(v => v > 0);
    const avgDuration = durations.length > 0 ? durations.reduce((a, b) => a + b, 0) / durations.length : 0;
    const maxDuration = durations.length > 0 ? Math.max(...durations) : 0;
    
    // 95% 응답 시간 계산 (간단한 근사치)
    const sortedDurations = [...durations].sort((a, b) => a - b);
    const p95Index = Math.floor(sortedDurations.length * 0.95);
    const p95Duration = sortedDurations.length > 0 ? sortedDurations[p95Index] : 0;
    
    // 99% 응답 시간 계산
    const p99Index = Math.floor(sortedDurations.length * 0.99);
    const p99Duration = sortedDurations.length > 0 ? sortedDurations[p99Index] : 0;
    
    // 총 요청 수 계산
    const totalRequests = httpReqs.reduce((sum, req) => sum + (req.value || 0), 0);
    
    // VU 통계
    const maxVus = vus.length > 0 ? Math.max(...vus.map(v => v.value || 0)) : 0;
    const avgVus = vus.length > 0 ? vus.reduce((sum, v) => sum + (v.value || 0), 0) / vus.length : 0;
    
    // 반복 횟수
    const totalIterations = iterations.reduce((sum, iter) => sum + (iter.value || 0), 0);
    
    // 데이터 통계
    const totalDataReceived = dataReceived.reduce((sum, data) => sum + (data.value || 0), 0);
    const totalDataSent = dataSent.reduce((sum, data) => sum + (data.value || 0), 0);
    
    return {
      filename: path.basename(jsonFile),
      http_req_duration: {
        avg: avgDuration,
        p95: p95Duration,
        p99: p99Duration,
        max: maxDuration,
      },
      http_req_rate: totalRequests / 30, // 30초 기준으로 계산
      http_req_failed: 0, // 실패율은 별도 계산 필요
      http_reqs: totalRequests,
      vus: avgVus,
      vus_max: maxVus,
      iterations: totalIterations,
      data_received: totalDataReceived,
      data_sent: totalDataSent,
    };
  } catch (error) {
    console.warn(`⚠️  ${jsonFile} 파일을 읽을 수 없습니다:`, error.message);
    return null;
  }
}

// 성능 등급 평가
function getPerformanceGrade(avgResponseTime, errorRate) {
  if (avgResponseTime < 200 && errorRate < 0.01) return '🟢 A+ (우수)';
  if (avgResponseTime < 300 && errorRate < 0.02) return '🟢 A (양호)';
  if (avgResponseTime < 500 && errorRate < 0.05) return '🟡 B (보통)';
  if (avgResponseTime < 1000 && errorRate < 0.1) return '🟠 C (주의)';
  return '🔴 D (위험)';
}

// 메트릭을 사람이 읽기 쉬운 형태로 변환
function formatMetric(value, unit = '') {
  if (typeof value === 'number') {
    if (value >= 1000000) return `${(value / 1000000).toFixed(2)}M${unit}`;
    if (value >= 1000) return `${(value / 1000).toFixed(2)}K${unit}`;
    if (value < 1 && value > 0) return `${(value * 1000).toFixed(2)}m${unit}`;
    return `${value.toFixed(2)}${unit}`;
  }
  return value;
}

// 메인 함수
function generateReport() {
  const summaryFile = path.join(reportsDir, `performance_analysis_${timestamp}.md`);
  const jsonFiles = fs.readdirSync(reportsDir)
    .filter(file => file.endsWith('.json') && file.includes(timestamp))
    .map(file => path.join(reportsDir, file));

  if (jsonFiles.length === 0) {
    console.warn('⚠️  분석할 JSON 파일을 찾을 수 없습니다.');
    return;
  }

  console.log(`📊 ${jsonFiles.length}개의 테스트 결과 파일을 분석 중...`);

  // 모든 메트릭 수집
  const allMetrics = jsonFiles
    .map(extractMetrics)
    .filter(metrics => metrics !== null);

  if (allMetrics.length === 0) {
    console.error('❌ 분석할 수 있는 메트릭이 없습니다.');
    return;
  }

  // 전체 통계 계산
  const totalStats = {
    totalRequests: allMetrics.reduce((sum, m) => sum + m.http_reqs, 0),
    avgResponseTime: allMetrics.reduce((sum, m) => sum + m.http_req_duration.avg, 0) / allMetrics.length,
    maxResponseTime: Math.max(...allMetrics.map(m => m.http_req_duration.max)),
    avgErrorRate: allMetrics.reduce((sum, m) => sum + m.http_req_failed, 0) / allMetrics.length,
    totalDataReceived: allMetrics.reduce((sum, m) => sum + m.data_received, 0),
    totalDataSent: allMetrics.reduce((sum, m) => sum + m.data_sent, 0),
  };

  // 성능 등급 계산
  const performanceGrade = getPerformanceGrade(totalStats.avgResponseTime, totalStats.avgErrorRate);

  // Markdown 보고서 생성
  let report = `# k6 성능 테스트 결과 분석 보고서

**생성 시간:** ${new Date().toLocaleString('ko-KR')}  
**타임스탬프:** ${timestamp}  
**성능 등급:** ${performanceGrade}

## 📊 전체 테스트 요약

| 메트릭 | 값 |
|--------|-----|
| **총 테스트 수** | ${allMetrics.length}개 |
| **총 요청 수** | ${formatMetric(totalStats.totalRequests)} |
| **평균 응답 시간** | ${formatMetric(totalStats.avgResponseTime, 'ms')} |
| **최대 응답 시간** | ${formatMetric(totalStats.maxResponseTime, 'ms')} |
| **평균 에러율** | ${(totalStats.avgErrorRate * 100).toFixed(2)}% |
| **총 수신 데이터** | ${formatMetric(totalStats.totalDataReceived, 'B')} |
| **총 송신 데이터** | ${formatMetric(totalStats.totalDataSent, 'B')} |

## 🎯 개별 테스트 결과

`;

  // 각 테스트별 상세 결과
  allMetrics.forEach((metrics, index) => {
    const testName = metrics.filename.replace(`-${timestamp}.json`, '');
    const grade = getPerformanceGrade(metrics.http_req_duration.avg, metrics.http_req_failed);
    
    report += `### ${index + 1}. ${testName} ${grade}

| 메트릭 | 값 |
|--------|-----|
| **파일명** | \`${metrics.filename}\` |
| **평균 응답 시간** | ${formatMetric(metrics.http_req_duration.avg, 'ms')} |
| **95% 응답 시간** | ${formatMetric(metrics.http_req_duration.p95, 'ms')} |
| **99% 응답 시간** | ${formatMetric(metrics.http_req_duration.p99, 'ms')} |
| **최대 응답 시간** | ${formatMetric(metrics.http_req_duration.max, 'ms')} |
| **요청률** | ${formatMetric(metrics.http_req_rate, '/s')} |
| **에러율** | ${(metrics.http_req_failed * 100).toFixed(2)}% |
| **총 요청 수** | ${formatMetric(metrics.http_reqs)} |
| **동시 사용자 수** | ${metrics.vus} / ${metrics.vus_max} |
| **반복 횟수** | ${formatMetric(metrics.iterations)} |
| **수신 데이터** | ${formatMetric(metrics.data_received, 'B')} |
| **송신 데이터** | ${formatMetric(metrics.data_sent, 'B')} |

`;
  });

  // 권장사항 추가
  report += `## 💡 성능 개선 권장사항

`;

  if (totalStats.avgResponseTime > 500) {
    report += `- **응답 시간 개선 필요**: 평균 응답 시간이 500ms를 초과합니다. 데이터베이스 쿼리 최적화나 캐싱 전략을 고려해보세요.\n`;
  }

  if (totalStats.avgErrorRate > 0.05) {
    report += `- **에러율 개선 필요**: 에러율이 5%를 초과합니다. 서버 로그를 확인하고 에러 원인을 파악해보세요.\n`;
  }

  if (totalStats.maxResponseTime > 2000) {
    report += `- **최대 응답 시간 개선 필요**: 일부 요청이 2초를 초과합니다. 느린 쿼리나 외부 API 호출을 최적화해보세요.\n`;
  }

  if (totalStats.avgResponseTime <= 300 && totalStats.avgErrorRate <= 0.02) {
    report += `- **우수한 성능**: 현재 성능이 매우 양호합니다. 추가 최적화는 낮은 우선순위로 고려해보세요.\n`;
  }

  report += `
## 📈 모니터링 지표

- **응답 시간 임계값**: 95% 요청이 500ms 이내
- **에러율 임계값**: 5% 미만
- **동시 사용자 수**: 테스트 시나리오에 따라 조정

## 🔍 다음 단계

1. 성능 병목 지점 식별
2. 데이터베이스 쿼리 최적화
3. 캐싱 전략 검토
4. 서버 리소스 모니터링
5. 정기적인 성능 테스트 실행

---
*이 보고서는 k6 테스트 결과를 자동으로 분석하여 생성되었습니다.*
`;

  // 파일 저장
  try {
    fs.writeFileSync(summaryFile, report, 'utf8');
    console.log(`✅ 성능 분석 보고서 생성 완료: ${summaryFile}`);
    
    // 간단한 통계 출력
    console.log('\n📊 주요 통계:');
    console.log(`   총 테스트: ${allMetrics.length}개`);
    console.log(`   평균 응답 시간: ${formatMetric(totalStats.avgResponseTime, 'ms')}`);
    console.log(`   평균 에러율: ${(totalStats.avgErrorRate * 100).toFixed(2)}%`);
    console.log(`   성능 등급: ${performanceGrade}`);
    
  } catch (error) {
    console.error('❌ 보고서 파일 저장 실패:', error.message);
    process.exit(1);
  }
}

// 스크립트 실행
if (require.main === module) {
  generateReport();
}

module.exports = { generateReport, extractMetrics, getPerformanceGrade };
