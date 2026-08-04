package com.pk.core.review;

public enum ReviewGuideScene {
    CREDIT_FAILED(ReviewGuideType.FAKE),
    ORDER_CREATED(ReviewGuideType.REAL),
    LOAN_PAID(ReviewGuideType.REAL);

    private final ReviewGuideType guideType;

    ReviewGuideScene(ReviewGuideType guideType) {
        this.guideType = guideType;
    }

    public ReviewGuideType guideType() {
        return guideType;
    }
}
