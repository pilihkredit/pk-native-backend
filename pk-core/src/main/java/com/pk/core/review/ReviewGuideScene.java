package com.pk.core.review;

public enum ReviewGuideScene {
    CREDIT_FAILED,
    ORDER_CREATED,
    LOAN_PAID;

    public boolean isStoreJumpScene() {
        return this == ORDER_CREATED || this == LOAN_PAID;
    }

    public static ReviewGuideScene fromName(String value) {
        return ReviewGuideScene.valueOf(value);
    }
}
