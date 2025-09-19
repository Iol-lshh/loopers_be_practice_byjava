package com.loopers.job.productmetric;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class ProductMetricAggregateJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    
    @Qualifier("productMetricAggregateJob")
    private final Job productMetricAggregateJob;

    @Override
    public void run(String... args) throws Exception {
        log.info("ProductMetricAggregateJob 시작 - {}", LocalDateTime.now());
        
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("executeTime", LocalDateTime.now().toString())
                    .toJobParameters();

            var jobExecution = jobLauncher.run(productMetricAggregateJob, jobParameters);
            
            log.info("ProductMetricAggregateJob 완료 - 상태: {}, 시간: {}", 
                    jobExecution.getStatus(), 
                    jobExecution.getEndTime());
                    
        } catch (Exception e) {
            log.error("ProductMetricAggregateJob 실행 중 오류 발생: {}", e.getMessage(), e);
            throw e;
        }
    }
}

/*
 * # 처음부터 실행 (startProductId 미지정 시 0부터 시작)
java -jar batch.jar --job.name=productMetricWeeklyAggregateJob

# 특정 상품 ID부터 실행 (예: 1000번 상품부터)
java -jar batch.jar --job.name=productMetricWeeklyAggregateJob startProductId=1000

---

# 주간 집계만 실행
java -jar batch.jar --job.name=productMetricAggregateJob stepType=WEEKLY

# 월간 집계만 실행
java -jar batch.jar --job.name=productMetricAggregateJob stepType=MONTHLY

# 둘 다 실행 (기본값)
java -jar batch.jar --job.name=productMetricAggregateJob stepType=BOTH

# 특정 상품 ID부터 주간 집계만 실행
java -jar batch.jar --job.name=productMetricAggregateJob stepType=WEEKLY startProductId=1000
 */
