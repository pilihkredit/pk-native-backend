package com.pk.core.review.port;

import com.pk.core.review.ReviewGuideAction;
import com.pk.core.review.ReviewGuideScene;
import com.pk.core.review.ReviewGuideType;

public interface ReviewGuideRepository {
    ClaimResult claim(long userId, ReviewGuideScene scene, ReviewGuideType guideType);

    ClickResult recordClick(long userId, long guideId, ReviewGuideAction action);

    void recordFeedback(long userId, long guideId, int rating);

    record ClaimResult(boolean shouldShow, Long guideId, ReviewGuideType guideType) {
        public static ClaimResult notAvailable() {
            return new ClaimResult(false, null, null);
        }
    }

    record ClickResult(boolean shouldOpenStore) {
    }
}
