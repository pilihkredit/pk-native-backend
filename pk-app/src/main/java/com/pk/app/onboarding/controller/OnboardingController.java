package com.pk.app.onboarding.controller;

import com.pk.app.common.web.ApiResponse;
import com.pk.app.common.web.RequestTrace;
import com.pk.app.onboarding.application.OnboardingApplicationService;
import com.pk.app.onboarding.dto.response.OnboardingProgressResponse;
import com.pk.app.security.SecurityContextSupport;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Onboarding progress.
 */
@RestController
@RequestMapping("/onboarding")
public class OnboardingController {
    private final OnboardingApplicationService onboardingApplicationService;

    public OnboardingController(OnboardingApplicationService onboardingApplicationService) {
        this.onboardingApplicationService = onboardingApplicationService;
    }

    /** Query onboarding completion progress. */
    @GetMapping("/progress")
    public ApiResponse<OnboardingProgressResponse> getProgress(HttpServletRequest httpRequest) {
        AuthenticatedPrincipal principal = SecurityContextSupport.requirePrincipal();
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        return ApiResponse.success(
                onboardingApplicationService.getProgress(principal),
                RequestTrace.resolveTraceId(httpRequest)
        );
    }
}
