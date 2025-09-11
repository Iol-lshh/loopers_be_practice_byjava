package com.loopers.application.ranking;

import com.loopers.domain.productmetric.ProductMetricInfo;
import com.loopers.domain.productmetric.ProductMetricService;
import com.loopers.domain.ranking.RankingCalculator;
import com.loopers.domain.ranking.RankingCommand;
import com.loopers.domain.ranking.RankingService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RankingFacade {
    private final RankingCalculator rankingCalculator;
    private final RankingService rankingService;
    private final ProductMetricService productMetricService;

    public void updateRanking(RankingCriteria.UpdateRanking criteria) {
        ProductMetricInfo.Aggregate info = productMetricService.findAggregate(
                Long.valueOf(criteria.id())
        ).orElseThrow(()-> new CoreException(ErrorType.NOT_FOUND));

        double score = rankingCalculator.calculate(info);
        RankingCommand.UpdateRanking updateCommand = new RankingCommand.UpdateRanking(
                criteria.id(),
                "product",
                score
        );
        rankingService.updateRanking(updateCommand);
    }
}
