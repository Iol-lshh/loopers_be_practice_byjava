package com.loopers.config.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.registry.EntryAddedEvent;
import io.github.resilience4j.core.registry.EntryRemovedEvent;
import io.github.resilience4j.core.registry.EntryReplacedEvent;
import io.github.resilience4j.core.registry.RegistryEventConsumer;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import java.time.Duration;

@Configuration
public class ResilienceConfig {
    private final static Logger LOG = LoggerFactory.getLogger(ResilienceConfig.class);

    @Bean
    public RegistryEventConsumer<CircuitBreaker> defaultCircuitBreakerEventConsumer() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<CircuitBreaker> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher().onEvent(event -> LOG.info(event.toString()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<CircuitBreaker> entryRemoveEvent) {

            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<CircuitBreaker> entryReplacedEvent) {

            }
        };
    }

    @Bean
    public RegistryEventConsumer<Retry> defaultRetryEventConsumer() {
        return new RegistryEventConsumer<>() {
            @Override
            public void onEntryAddedEvent(EntryAddedEvent<Retry> entryAddedEvent) {
                entryAddedEvent.getAddedEntry().getEventPublisher().onEvent(event -> LOG.info(event.toString()));
            }

            @Override
            public void onEntryRemovedEvent(EntryRemovedEvent<Retry> entryRemoveEvent) {

            }

            @Override
            public void onEntryReplacedEvent(EntryReplacedEvent<Retry> entryReplacedEvent) {

            }
        };
    }

    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry(Environment env) {
        CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
                .slidingWindowSize(env.getProperty("resilience4j.circuitbreaker.configs.default.sliding-window-size", Integer.class, 10))
                .minimumNumberOfCalls(env.getProperty("resilience4j.circuitbreaker.configs.default.minimum-number-of-calls", Integer.class, 5))
                .failureRateThreshold(env.getProperty("resilience4j.circuitbreaker.configs.default.failure-rate-threshold", Float.class, 50.0f))
                .waitDurationInOpenState(Duration.ofMillis(env.getProperty("resilience4j.circuitbreaker.configs.default.wait-duration-in-open-state", Long.class, 5000L)))
                .permittedNumberOfCallsInHalfOpenState(env.getProperty("resilience4j.circuitbreaker.configs.default.permitted-number-of-calls-in-half-open-state", Integer.class, 3))
                .build();

        return CircuitBreakerRegistry.of(circuitBreakerConfig);
    }

    @Bean
    public RetryRegistry retryRegistry(Environment env) {
        return RetryRegistry.ofDefaults();
    }

    @Bean
    public TimeLimiterRegistry timeLimiterRegistry(Environment env) {
        TimeLimiterConfig timeLimiterConfig = TimeLimiterConfig.custom()
                .cancelRunningFuture(env.getProperty("resilience4j.timelimiter.configs.default.cancel-running-future", Boolean.class, true))
                .timeoutDuration(Duration.ofMillis(env.getProperty("resilience4j.timelimiter.configs.default.timeout-duration", Long.class, 300L)))
                .build();
        
        return TimeLimiterRegistry.of(timeLimiterConfig);
    }
}
