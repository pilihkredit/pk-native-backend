package com.pk.infra.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.review.ReviewGuideAction;
import com.pk.core.review.ReviewGuideScene;
import com.pk.core.review.ReviewGuideType;
import com.pk.infra.review.mapper.ReviewGuideMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewGuideRepositoryImplTest {
    private ReviewGuideMapper mapper;
    private ReviewGuideRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        mapper = mock(ReviewGuideMapper.class);
        repository = new ReviewGuideRepositoryImpl(mapper);
        when(mapper.findUserStateForUpdate(10L)).thenReturn(new ReviewGuideUserStateRow(10L, null));
    }

    @Test
    void claimCreatesOneExposureForAnEligibleUserAndScene() {
        doAnswer(invocation -> {
            ReviewGuideExposureInsertParam param = invocation.getArgument(0);
            param.setId(25L);
            return 1;
        }).when(mapper).insertExposure(any(ReviewGuideExposureInsertParam.class));

        var result = repository.claim(10L, ReviewGuideScene.CREDIT_FAILED, ReviewGuideType.FAKE);

        assertThat(result.shouldShow()).isTrue();
        assertThat(result.guideId()).isEqualTo(25L);
        assertThat(result.guideType()).isEqualTo(ReviewGuideType.FAKE);
        verify(mapper).ensureUserState(10L);
        verify(mapper).findByUserIdAndScene(10L, "CREDIT_FAILED");
    }

    @Test
    void claimDoesNotShowAfterARealReviewClick() {
        when(mapper.findUserStateForUpdate(10L)).thenReturn(
                new ReviewGuideUserStateRow(10L, Instant.parse("2026-08-05T01:00:00Z"))
        );

        var result = repository.claim(10L, ReviewGuideScene.LOAN_PAID, ReviewGuideType.REAL);

        assertThat(result.shouldShow()).isFalse();
        verify(mapper, org.mockito.Mockito.never()).insertExposure(any());
    }

    @Test
    void realClickClaimsTheGlobalRealReviewSlot() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "REAL", null)
        );
        when(mapper.markRealReviewClicked(10L)).thenReturn(1);

        var result = repository.recordClick(10L, 25L, ReviewGuideAction.RATE);

        assertThat(result.shouldOpenStore()).isTrue();
        verify(mapper).markExposureClicked(25L);
    }

    @Test
    void rejectsAnActionThatDoesNotMatchTheGuideType() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "FAKE", null)
        );

        assertThatThrownBy(() -> repository.recordClick(10L, 25L, ReviewGuideAction.RATE))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    @Test
    void feedbackIsStoredOnlyForFakeGuides() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "FAKE", null)
        );

        repository.recordFeedback(10L, 25L, 5);

        verify(mapper).saveRatingIfAbsent(25L, 5);
    }
}
