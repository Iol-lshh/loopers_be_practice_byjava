package com.loopers.application.ranking;

public class RankingCriteria {
    public record UpdateRanking(
            String id,
            String type,
            String metricType,
            String value
    ) {
    }
}
