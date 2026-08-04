package com.pk.infra.review.repository;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.review.ReviewGuideAction;
import com.pk.core.review.ReviewGuideScene;
import com.pk.core.review.ReviewGuideType;
import com.pk.core.review.port.ReviewGuideRepository;
import com.pk.infra.review.mapper.ReviewGuideMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReviewGuideRepositoryImpl implements ReviewGuideRepository {
    private final ReviewGuideMapper reviewGuideMapper;

    public ReviewGuideRepositoryImpl(ReviewGuideMapper reviewGuideMapper) {
        this.reviewGuideMapper = reviewGuideMapper;
    }

    @Override
    @Transactional
    public ClaimResult claim(long userId, ReviewGuideScene scene, ReviewGuideType guideType) {
        ReviewGuideUserStateRow state = lockUserState(userId);
        if (state.realReviewClickedAt() != null
                || reviewGuideMapper.findByUserIdAndScene(userId, scene.name()) != null) {
            return ClaimResult.notAvailable();
        }

        ReviewGuideExposureInsertParam param = new ReviewGuideExposureInsertParam();
        param.setUserId(userId);
        param.setScene(scene.name());
        param.setGuideType(guideType.name());
        reviewGuideMapper.insertExposure(param);
        return new ClaimResult(true, param.getId(), guideType);
    }

    @Override
    @Transactional
    public ClickResult recordClick(long userId, long guideId, ReviewGuideAction action) {
        ReviewGuideUserStateRow state = lockUserState(userId);
        ReviewGuideExposureRow exposure = reviewGuideMapper.findByIdAndUserIdForUpdate(guideId, userId);
        if (exposure == null || !matchesAction(exposure.guideType(), action)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        if (ReviewGuideAction.FAKE_CLICK == action) {
            reviewGuideMapper.markExposureClicked(guideId);
            return new ClickResult(false);
        }
        if (state.realReviewClickedAt() != null) {
            return new ClickResult(false);
        }

        int updated = reviewGuideMapper.markRealReviewClicked(userId);
        if (updated == 0) {
            return new ClickResult(false);
        }
        reviewGuideMapper.markExposureClicked(guideId);
        return new ClickResult(true);
    }

    @Override
    @Transactional
    public void recordFeedback(long userId, long guideId, int rating) {
        lockUserState(userId);
        ReviewGuideExposureRow exposure = reviewGuideMapper.findByIdAndUserIdForUpdate(guideId, userId);
        if (exposure == null || !ReviewGuideType.FAKE.name().equals(exposure.guideType())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        reviewGuideMapper.saveRatingIfAbsent(guideId, rating);
    }

    private ReviewGuideUserStateRow lockUserState(long userId) {
        reviewGuideMapper.ensureUserState(userId);
        ReviewGuideUserStateRow state = reviewGuideMapper.findUserStateForUpdate(userId);
        if (state == null) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return state;
    }

    private static boolean matchesAction(String guideType, ReviewGuideAction action) {
        return (ReviewGuideType.FAKE.name().equals(guideType) && action == ReviewGuideAction.FAKE_CLICK)
                || (ReviewGuideType.REAL.name().equals(guideType) && action == ReviewGuideAction.RATE);
    }
}
