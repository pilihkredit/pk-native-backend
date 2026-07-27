package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record TrustDecisionLivenessLicenseRequest(
        @Min(1) @Max(86_400) Integer sessionDurationSeconds
) {
}
