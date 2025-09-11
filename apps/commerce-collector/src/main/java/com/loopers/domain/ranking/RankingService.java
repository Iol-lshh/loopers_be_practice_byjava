package com.loopers.domain.ranking;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RankingService {
    private final RankingCacheRepository rankingCacheRepository;

    public void updateRanking(RankingCommand.UpdateRanking updateCommand) {
        rankingCacheRepository.put(updateCommand);
    }

    public void addLastDayRanking() {
        rankingCacheRepository.addLastDayRanking();
    }
}
