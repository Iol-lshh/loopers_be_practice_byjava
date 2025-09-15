package com.loopers.application.ranking;

import com.loopers.domain.productmetric.ProductMetricInfo;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.domain.ranking.RankingCalculator;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Component
public class RankingFacade {
    private final RankingCalculator rankingCalculator;
    private final RankingService rankingService;
    private final ProductMetricService productMetricService;

    public void updateAllProductToDayRankings() {
        log.info("모든 상품 랭킹 업데이트 작업을 시작합니다.");
        
        // 배치 시작 시 전날 랭킹 데이터를 한 번만 추가
        log.info("전날 랭킹 데이터를 오늘 랭킹에 추가합니다.");
        try {
            rankingService.addLastDayRanking();
            log.info("전날 랭킹 데이터 추가가 완료되었습니다.");
        } catch (Exception e) {
            log.error("전날 랭킹 데이터 추가 중 오류가 발생했습니다: {}", e.getMessage());
        }

        List<Long> productIds = productMetricService.findAllDistinctProductIds();
        log.info("총 {}개의 상품에 대해 랭킹을 업데이트합니다.", productIds.size());

        int successCount = 0;
        int failureCount = 0;

        for (Long productId : productIds) {
            try {
                ProductMetricInfo.Aggregate info = productMetricService.findAggregate(productId)
                        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));

                double score = rankingCalculator.calculate(info);
                RankingCommand.UpdateRanking updateCommand = new RankingCommand.UpdateRanking(
                        String.valueOf(productId),
                        "product",
                        score
                );
                rankingService.updateRanking(updateCommand);
                successCount++;

            } catch (Exception e) {
                log.error("상품 ID {}의 랭킹 업데이트 중 오류가 발생했습니다: {}", productId, e.getMessage());
                failureCount++;
            }
        }

        log.info("모든 상품 랭킹 업데이트 작업이 완료되었습니다. 성공: {}, 실패: {}", successCount, failureCount);
    }

    public void updateAllProductRankings() {
        log.info("모든 상품 랭킹 업데이트 작업을 시작합니다.");
        
        List<Long> productIds = productMetricService.findAllDistinctProductIds();
        log.info("총 {}개의 상품에 대해 랭킹을 업데이트합니다.", productIds.size());
        
        int successCount = 0;
        int failureCount = 0;
        
        for (Long productId : productIds) {
            try {
                ProductMetricInfo.Aggregate info = productMetricService.findAggregate(productId)
                        .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
                
                double score = rankingCalculator.calculate(info);
                RankingCommand.UpdateRanking updateCommand = new RankingCommand.UpdateRanking(
                        String.valueOf(productId),
                        "product",
                        score
                );
                rankingService.updateRanking(updateCommand);
                successCount++;
                
            } catch (Exception e) {
                log.error("상품 ID {}의 랭킹 업데이트 중 오류가 발생했습니다: {}", productId, e.getMessage());
                failureCount++;
            }
        }
        
        log.info("모든 상품 랭킹 업데이트 작업이 완료되었습니다. 성공: {}, 실패: {}", successCount, failureCount);
    }
}
