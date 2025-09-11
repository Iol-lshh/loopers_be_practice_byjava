package com.loopers.application.ranking;

import com.loopers.domain.productmetric.ProductMetricEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
public class RankingEventHandler {
    private final RankingFacade rankingFacade;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductMetricEvent.UpdateLikeCount event){

        RankingCriteria.UpdateRanking command = new RankingCriteria.UpdateRanking(
                event.productId().toString(),
                "PRODUCT",
                "LIKE_COUNT",
                event.likeCount().toString()
        );
        rankingFacade.updateRanking(command);
    }

    // todo sold_count, view_count
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(ProductMetricEvent.UpdateSoldCount event){

        RankingCriteria.UpdateRanking command = new RankingCriteria.UpdateRanking(
                event.productId().toString(),
                "PRODUCT",
                "SOLD_COUNT",
                event.soldCount().toString()
        );
        rankingFacade.updateRanking(command);
    }

}
