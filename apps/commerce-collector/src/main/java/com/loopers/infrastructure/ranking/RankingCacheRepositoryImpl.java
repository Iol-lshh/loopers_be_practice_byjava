package com.loopers.infrastructure.ranking;

import com.loopers.domain.ranking.RankingCacheRepository;
import com.loopers.domain.ranking.RankingCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Set;

@Slf4j
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

    @Override
    public void addLastDayRanking() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDate today = LocalDate.now();
        String type = "product";
        
        String yesterdayKey = type + ":ranking:" + yesterday;
        String todayKey = type + ":ranking:" + today;
        
        log.info("전날({}) 랭킹 데이터를 오늘({}) 랭킹에 1/10 스코어로 추가합니다.", yesterday, today);
        
        // 전날 랭킹 데이터 가져오기
        Set<String> yesterdayMembers = redisTemplate.opsForZSet().range(yesterdayKey, 0, -1);
        
        if (yesterdayMembers == null || yesterdayMembers.isEmpty()) {
            log.warn("전날({}) 랭킹 데이터가 없습니다.", yesterday);
            return;
        }
        
        int addedCount = 0;
        for (String member : yesterdayMembers) {
            Double yesterdayScore = redisTemplate.opsForZSet().score(yesterdayKey, member);
            if (yesterdayScore != null) {
                // 전날 스코어의 1/10을 오늘 스코어에 추가
                double adjustedScore = yesterdayScore / 10.0;
                redisTemplate.opsForZSet().incrementScore(todayKey, member, adjustedScore);
                addedCount++;
                
                log.debug("상품 ID: {}, 전날 스코어: {}, 추가된 스코어: {}", member, yesterdayScore, adjustedScore);
            }
        }
        
        log.info("전날 랭킹 데이터 추가 완료. 총 {}개 상품의 스코어가 추가되었습니다.", addedCount);
    }

    @Override
    public Set<String> getRankingByDate(LocalDate date, String type) {
        String key = type + ":ranking:" + date;
        return redisTemplate.opsForZSet().range(key, 0, -1);
    }

    @Override
    public Double getScoreByDate(LocalDate date, String type, String member) {
        String key = type + ":ranking:" + date;
        Double score = redisTemplate.opsForZSet().score(key, member);
        return score != null ? score : 0.0;
    }
}
