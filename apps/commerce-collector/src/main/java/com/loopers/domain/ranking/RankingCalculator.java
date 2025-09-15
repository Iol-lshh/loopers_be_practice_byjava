package com.loopers.domain.ranking;

import com.loopers.domain.productmetric.ProductMetricInfo;
import org.springframework.stereotype.Component;

@Component
public class RankingCalculator {
    public double calculate(ProductMetricInfo.Aggregate info) {
        double viewScore = calculateView(info.viewCount());
        double likeScore = calculateLike(info.likeCount());
        double soldScore = calculateSold(info.soldCount(), info.soldAmount());
        return viewScore + likeScore + soldScore;
    }

    private double calculateView(Long viewCount) {
        double weight = 0.1;
        return viewCount * weight;
    }

    private double calculateLike(Long likeCount) {
        double weight = 0.2;
        return likeCount * weight;
    }

    private double calculateSold(Long soldCount, Long price) {
        double weight = 0.7;
        return soldCount * price * weight;
    }
}
