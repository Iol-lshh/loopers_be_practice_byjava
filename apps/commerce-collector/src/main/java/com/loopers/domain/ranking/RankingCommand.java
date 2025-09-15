package com.loopers.domain.ranking;

public class RankingCommand {
    public record UpdateRanking(
            String id,
            String type,
            double score
    ) {
    }
}
