package com.pk.infra.review.repository;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.review.ReviewGuideScene;
import com.pk.core.review.port.ReviewGuideRepository;
import com.pk.infra.review.mapper.ReviewGuideMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ReviewGuideRepositoryImpl implements ReviewGuideRepository {
    static final String GUIDE_TYPE_IN_APP = "IN_APP";

    private final ReviewGuideMapper reviewGuideMapper;

    public ReviewGuideRepositoryImpl(ReviewGuideMapper reviewGuideMapper) {
        this.reviewGuideMapper = reviewGuideMapper;
    }

    @Override
    @Transactional
    public ClaimResult claim(long userId, ReviewGuideScene scene) {
        ReviewGuideUserStateRow state = lockUserState(userId);
        if (state.storeJumpAt() != null
                || reviewGuideMapper.findByUserIdAndScene(userId, scene.name()) != null) {
            return ClaimResult.notAvailable();
        }

        ReviewGuideExposureInsertParam param = new ReviewGuideExposureInsertParam();
        param.setUserId(userId);
        param.setScene(scene.name());
        param.setGuideType(GUIDE_TYPE_IN_APP);
        reviewGuideMapper.insertExposure(param);
        return new ClaimResult(true, param.getId());
    }

    @Override
    @Transactional
    public void recordFeedback(long userId, long guideId, int rating, int minJumpRating) {
        lockUserState(userId);
        ReviewGuideExposureRow exposure = reviewGuideMapper.findByIdAndUserIdForUpdate(guideId, userId);
        if (exposure == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        reviewGuideMapper.saveRatingIfAbsent(guideId, rating);

        ReviewGuideScene scene;
        try {
            scene = ReviewGuideScene.fromName(exposure.scene());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if (scene.isStoreJumpScene() && rating >= minJumpRating) {
            reviewGuideMapper.markStoreJumpIfAbsent(userId);
        }
    }

    private ReviewGuideUserStateRow lockUserState(long userId) {
        reviewGuideMapper.ensureUserState(userId);
        ReviewGuideUserStateRow state = reviewGuideMapper.findUserStateForUpdate(userId);
        if (state == null) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return state;
    }
}
