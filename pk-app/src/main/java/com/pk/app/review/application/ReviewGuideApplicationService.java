package com.pk.app.review.application;

import com.pk.app.review.dto.request.ReviewGuideClaimRequest;
import com.pk.app.review.dto.request.ReviewGuideFeedbackRequest;
import com.pk.app.review.dto.response.ReviewGuideClaimResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.review.ReviewGuideScene;
import com.pk.infra.review.ReviewGuideFacade;

public class ReviewGuideApplicationService {
    private final ReviewGuideFacade reviewGuideFacade;

    public ReviewGuideApplicationService(ReviewGuideFacade reviewGuideFacade) {
        this.reviewGuideFacade = reviewGuideFacade;
    }

    public ReviewGuideClaimResponse claim(AuthenticatedPrincipal principal, ReviewGuideClaimRequest request) {
        var result = reviewGuideFacade.claim(requirePrincipal(principal).userId(), parseScene(request.scene()));
        return new ReviewGuideClaimResponse(result.shouldShow(), result.guideId());
    }

    public void feedback(AuthenticatedPrincipal principal, ReviewGuideFeedbackRequest request) {
        reviewGuideFacade.recordFeedback(
                requirePrincipal(principal).userId(),
                request.guideId(),
                request.rating()
        );
    }

    private static AuthenticatedPrincipal requirePrincipal(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return principal;
    }

    private static ReviewGuideScene parseScene(String value) {
        try {
            return ReviewGuideScene.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }
}
