package com.pk.app.review.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.pk.app.review.dto.request.ReviewGuideClaimRequest;
import com.pk.app.review.dto.request.ReviewGuideClickRequest;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.review.ReviewGuideAction;
import com.pk.core.review.ReviewGuideType;
import com.pk.core.review.port.ReviewGuideRepository;
import com.pk.infra.review.ReviewGuideFacade;
import org.junit.jupiter.api.Test;

class ReviewGuideApplicationServiceTest {
    @Test
    void claimReturnsTheGuideIdentifierAndType() {
        ReviewGuideFacade facade = mock(ReviewGuideFacade.class);
        when(facade.claim(anyLong(), any())).thenReturn(
                new ReviewGuideRepository.ClaimResult(true, 25L, ReviewGuideType.FAKE)
        );
        ReviewGuideApplicationService service = new ReviewGuideApplicationService(facade);

        var response = service.claim(principal(), new ReviewGuideClaimRequest("CREDIT_FAILED"));

        assertThat(response.shouldShow()).isTrue();
        assertThat(response.guideId()).isEqualTo(25L);
        assertThat(response.guideType()).isEqualTo("FAKE");
    }

    @Test
    void clickPassesTheGuideIdentifierAndActionToTheFacade() {
        ReviewGuideFacade facade = mock(ReviewGuideFacade.class);
        when(facade.recordClick(10L, 25L, ReviewGuideAction.RATE)).thenReturn(
                new ReviewGuideRepository.ClickResult(true)
        );
        ReviewGuideApplicationService service = new ReviewGuideApplicationService(facade);

        var response = service.click(principal(), new ReviewGuideClickRequest(25L, "RATE"));

        assertThat(response.shouldOpenStore()).isTrue();
        verify(facade).recordClick(10L, 25L, ReviewGuideAction.RATE);
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
