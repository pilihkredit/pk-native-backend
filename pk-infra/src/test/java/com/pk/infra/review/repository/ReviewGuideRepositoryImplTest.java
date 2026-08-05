package com.pk.infra.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.review.ReviewGuideScene;
import com.pk.infra.review.mapper.ReviewGuideMapper;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReviewGuideRepositoryImplTest {
    private static final int MIN_JUMP_RATING = 4;

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

        var result = repository.claim(10L, ReviewGuideScene.CREDIT_FAILED);

        assertThat(result.shouldShow()).isTrue();
        assertThat(result.guideId()).isEqualTo(25L);
        verify(mapper).ensureUserState(10L);
        verify(mapper).findByUserIdAndScene(10L, "CREDIT_FAILED");
        verify(mapper).insertExposure(any(ReviewGuideExposureInsertParam.class));
    }

    @Test
    void claimDoesNotShowAfterStoreJumpMarked() {
        when(mapper.findUserStateForUpdate(10L)).thenReturn(
                new ReviewGuideUserStateRow(10L, Instant.parse("2026-08-05T01:00:00Z"))
        );

        var result = repository.claim(10L, ReviewGuideScene.CREDIT_FAILED);

        assertThat(result.shouldShow()).isFalse();
        verify(mapper, never()).insertExposure(any());
    }

    @Test
    void feedbackIsStoredForAnyScene() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "ORDER_CREATED", null)
        );

        repository.recordFeedback(10L, 25L, 3, MIN_JUMP_RATING);

        verify(mapper).saveRatingIfAbsent(25L, 3);
        verify(mapper, never()).markStoreJumpIfAbsent(10L);
    }

    @Test
    void highRatingOnOrderCreatedMarksStoreJumpAndSuppressesFutureClaims() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "ORDER_CREATED", null)
        );

        repository.recordFeedback(10L, 25L, MIN_JUMP_RATING, MIN_JUMP_RATING);

        verify(mapper).saveRatingIfAbsent(25L, MIN_JUMP_RATING);
        verify(mapper).markStoreJumpIfAbsent(10L);
    }

    @Test
    void highRatingOnLoanPaidMarksStoreJump() {
        when(mapper.findByIdAndUserIdForUpdate(26L, 10L)).thenReturn(
                new ReviewGuideExposureRow(26L, "LOAN_PAID", null)
        );

        repository.recordFeedback(10L, 26L, 5, MIN_JUMP_RATING);

        verify(mapper).markStoreJumpIfAbsent(10L);
    }

    @Test
    void highRatingOnCreditFailedDoesNotMarkStoreJump() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(
                new ReviewGuideExposureRow(25L, "CREDIT_FAILED", null)
        );

        repository.recordFeedback(10L, 25L, 5, MIN_JUMP_RATING);

        verify(mapper).saveRatingIfAbsent(25L, 5);
        verify(mapper, never()).markStoreJumpIfAbsent(10L);
    }

    @Test
    void feedbackRejectsMissingExposure() {
        when(mapper.findByIdAndUserIdForUpdate(25L, 10L)).thenReturn(null);

        assertThatThrownBy(() -> repository.recordFeedback(10L, 25L, 5, MIN_JUMP_RATING))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }
}
