package com.pk.app.profile.dto.response;

public record TrustDecisionLivenessLicenseResponse(
        String license,
        long expiryTimestamp,
        String sequenceId
) {
}
