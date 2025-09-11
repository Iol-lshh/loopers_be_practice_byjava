package com.loopers.domain.ranking;

import java.time.LocalDate;
import java.util.Set;

public interface RankingCacheRepository {
    void put(RankingCommand.UpdateRanking updateCommand);

    /**
     * 전날 랭킹 데이터를 오늘 랭킹에 1/10 스코어로 추가합니다.
     */
    void addLastDayRanking();
    
    /**
     * 특정 날짜의 랭킹 데이터를 가져옵니다.
     * @param date 조회할 날짜
     * @param type 랭킹 타입 (예: "product")
     * @return 랭킹 데이터 (member, score 쌍)
     */
    Set<String> getRankingByDate(LocalDate date, String type);
    
    /**
     * 특정 날짜의 특정 멤버 스코어를 가져옵니다.
     * @param date 조회할 날짜
     * @param type 랭킹 타입
     * @param member 멤버 ID
     * @return 스코어값, 없으면 0.0
     */
    Double getScoreByDate(LocalDate date, String type, String member);
}
