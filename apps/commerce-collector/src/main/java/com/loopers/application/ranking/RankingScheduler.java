package com.loopers.application.ranking;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingScheduler {

    private final RankingFacade rankingFacade;

    @Scheduled(cron = "0 0 2 * * *")
    public void updateAllProductRankingsBatch() {
        log.info("스케줄된 모든 상품 랭킹 업데이트 작업을 시작합니다.");
        try {
            rankingFacade.updateAllProductToDayRankings();
            log.info("스케줄된 모든 상품 랭킹 업데이트 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("스케줄된 모든 상품 랭킹 업데이트 작업 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }

    @Scheduled(fixedRate = 1800000) // 30분 = 30 * 60 * 1000ms
    public void updateAllProductRankingsPeriodic() {
        log.info("정기적인 모든 상품 랭킹 업데이트 작업을 시작합니다.");
        try {
            rankingFacade.updateAllProductRankings();
            log.info("정기적인 모든 상품 랭킹 업데이트 작업이 성공적으로 완료되었습니다.");
        } catch (Exception e) {
            log.error("정기적인 모든 상품 랭킹 업데이트 작업 중 오류가 발생했습니다: {}", e.getMessage(), e);
        }
    }
}
