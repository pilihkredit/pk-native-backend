package com.pk.core.review.port;

import com.pk.core.review.ReviewGuideScene;

public interface ReviewGuideRepository {
    ClaimResult claim(long userId, ReviewGuideScene scene);

    void recordFeedback(long userId, long guideId, int rating, int minJumpRating);

    record ClaimResult(boolean shouldShow, Long guideId) {
        public static ClaimResult notAvailable() {
            return new ClaimResult(false, null);
        }
    }
}
