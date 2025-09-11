package com.loopers.domain.ranking;

public interface RankingCacheRepository {
    void put(RankingCommand.UpdateRanking updateCommand);
}
