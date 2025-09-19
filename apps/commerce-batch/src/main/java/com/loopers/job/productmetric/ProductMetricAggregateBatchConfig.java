package com.loopers.job.productmetric;

import com.loopers.domain.productmetric.ProductMetricMonthlyAggregated;
import com.loopers.domain.productmetric.ProductMetricReader;
import com.loopers.domain.productmetric.ProductMetricWeeklyAggregated;
import com.loopers.domain.productmetric.ProductMetricWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@RequiredArgsConstructor
@Configuration
public class ProductMetricAggregateBatchConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ProductMetricReader productMetricReader;
    private final ProductMetricWriter productMetricWriter;

    @Bean
    public Job productMetricAggregateJob() {
        return new JobBuilder("productMetricAggregateJob", jobRepository)
                .start(stepDecider()).on("WEEKLY").to(weeklyAggregateStep()).next(monthlyAggregateStep()).next(cacheRankingStep())
                .from(stepDecider()).on("MONTHLY").to(monthlyAggregateStep()).next(cacheRankingStep())
                .end()
                .build();
    }

    @Bean
    public Step weeklyAggregateStep() {
        return new StepBuilder("weeklyAggregateStep", jobRepository)
                .<ProductMetricWeeklyAggregated, ProductMetricWeeklyAggregated>chunk(1000, transactionManager)
                .reader(productMetricReader.weeklyMetricReader(null))
                .writer(productMetricWriter.weeklyWriter())
                .build();
    }

    @Bean
    public Step monthlyAggregateStep() {
        return new StepBuilder("monthlyAggregateStep", jobRepository)
                .<ProductMetricMonthlyAggregated, ProductMetricMonthlyAggregated>chunk(1000, transactionManager)
                .reader(productMetricReader.monthlyMetricReader(null))
                .writer(productMetricWriter.monthlyWriter())
                .build();
    }

    @Bean
    public Step cacheRankingStep() {
        return new StepBuilder("cacheRankingStep", jobRepository)
                .tasklet(cacheRankingTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet cacheRankingTasklet() {
        return (contribution, chunkContext) -> {
            productMetricWriter.cacheRanking();
            return RepeatStatus.FINISHED;
        };
    }



    @Bean
    @StepScope
    public JobExecutionDecider stepDecider() {
        return new ProductMetricStepDecider();
    }

    private static class ProductMetricStepDecider implements JobExecutionDecider {
        @Override
        public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
            String stepType = jobExecution.getJobParameters().getString("stepType");
            
            if (stepType == null || stepType.isEmpty()) {
                return new FlowExecutionStatus("WEEKLY");
            }
            
            return switch (stepType.toUpperCase()) {
                case "MONTHLY" -> new FlowExecutionStatus("MONTHLY");
                default -> new FlowExecutionStatus("WEEKLY");
            };
        }
    }
}
