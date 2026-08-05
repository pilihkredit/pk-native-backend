package com.pk.app.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.review.dto.request.ReviewGuideClaimRequest;
import com.pk.app.review.dto.request.ReviewGuideFeedbackRequest;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.review.port.ReviewGuideRepository;
import com.pk.infra.review.ReviewGuideFacade;
import org.junit.jupiter.api.Test;

class ReviewGuideApplicationServiceTest {
    @Test
    void claimReturnsTheGuideIdentifier() {
        ReviewGuideFacade facade = mock(ReviewGuideFacade.class);
        when(facade.claim(anyLong(), any())).thenReturn(
                new ReviewGuideRepository.ClaimResult(true, 25L)
        );
        ReviewGuideApplicationService service = new ReviewGuideApplicationService(facade);

        var response = service.claim(principal(), new ReviewGuideClaimRequest("CREDIT_FAILED"));

        assertThat(response.shouldShow()).isTrue();
        assertThat(response.guideId()).isEqualTo(25L);
    }

    @Test
    void feedbackPassesGuideIdAndRatingToTheFacade() {
        ReviewGuideFacade facade = mock(ReviewGuideFacade.class);
        ReviewGuideApplicationService service = new ReviewGuideApplicationService(facade);

        service.feedback(principal(), new ReviewGuideFeedbackRequest(25L, 4));

        verify(facade).recordFeedback(10L, 25L, 4);
    }

    @Test
    void claimRejectsUnknownScenes() {
        ReviewGuideApplicationService service = new ReviewGuideApplicationService(mock(ReviewGuideFacade.class));

        assertThatThrownBy(() -> service.claim(principal(), new ReviewGuideClaimRequest("UNKNOWN")))
                .isInstanceOf(ApiException.class)
                .extracting("apiCode")
                .isEqualTo(ApiCode.INVALID_REQUEST_PARAMETERS);
    }

    private static AuthenticatedPrincipal principal() {
        return new AuthenticatedPrincipal(10L, "U10001", "81234567890", 1L);
    }
}
