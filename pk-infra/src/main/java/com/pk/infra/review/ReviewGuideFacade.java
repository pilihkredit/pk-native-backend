package com.pk.infra.review;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.review.ReviewGuideAction;
import com.pk.core.review.ReviewGuideScene;
import com.pk.core.review.ReviewGuideType;
import com.pk.core.review.port.ReviewGuideRepository;

public class ReviewGuideFacade {
    private final ReviewGuideRepository reviewGuideRepository;

    public ReviewGuideFacade(ReviewGuideRepository reviewGuideRepository) {
        this.reviewGuideRepository = reviewGuideRepository;
    }

    public ReviewGuideRepository.ClaimResult claim(long userId, ReviewGuideScene scene) {
        requireUserId(userId);
        if (scene == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return reviewGuideRepository.claim(userId, scene, scene.guideType());
    }

    public ReviewGuideRepository.ClickResult recordClick(long userId, long guideId, ReviewGuideAction action) {
        requireUserId(userId);
        if (guideId <= 0L || action == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return reviewGuideRepository.recordClick(userId, guideId, action);
    }

    public void recordFeedback(long userId, long guideId, int rating) {
        requireUserId(userId);
        if (guideId <= 0L || rating < 1 || rating > 5) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        reviewGuideRepository.recordFeedback(userId, guideId, rating);
    }

    private static void requireUserId(long userId) {
        if (userId <= 0L) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }
}
