package com.pk.app.onboarding.dto.response;

import java.util.List;

public record OnboardingProgressResponse(
        String partnerUserId,
        String kycStatus,
        List<String> completedModules,
        List<String> missingModules
) {
}
