package com.pk.app.onboarding.application;

import com.pk.app.onboarding.dto.response.OnboardingProgressResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.infra.profile.OnboardingProgressFacade;
import org.springframework.stereotype.Service;

@Service
public class OnboardingApplicationService {
    private final OnboardingProgressFacade onboardingProgressFacade;

    public OnboardingApplicationService(OnboardingProgressFacade onboardingProgressFacade) {
        this.onboardingProgressFacade = onboardingProgressFacade;
    }

    public OnboardingProgressResponse getProgress(AuthenticatedPrincipal principal) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        OnboardingProgressFacade.OnboardingProgressResult result = onboardingProgressFacade.getProgress(
                principal.userId(),
                principal.partnerUserId()
        );
        return new OnboardingProgressResponse(
                result.partnerUserId(),
                result.kycStatus(),
                result.completedModules(),
                result.missingModules()
        );
    }
}
