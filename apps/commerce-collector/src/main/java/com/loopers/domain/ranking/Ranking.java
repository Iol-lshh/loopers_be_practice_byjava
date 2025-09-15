package com.loopers.domain.ranking;

import lombok.Getter;

@Getter
public class Ranking {
    private String key;
    private String value;
    private String rankingValue;
    private Type type;
    private DurationType durationType;

    public enum Type {
        PRODUCT
    }

    public enum DurationType {
        DAILY,
        WEEKLY,
        MONTHLY
    }
}
