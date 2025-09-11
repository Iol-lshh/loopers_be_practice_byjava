package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCacheRepository;
import com.loopers.domain.ranking.RankingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@RequiredArgsConstructor
@Component
public class RankingCacheRepositoryImpl implements RankingCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void put(RankingCommand.UpdateRanking updateCommand) {
        String key = updateCommand.type() + ":ranking:" +
                LocalDate.now();
        redisTemplate.opsForZSet().add(key, updateCommand.id(), updateCommand.score());
    }
}
