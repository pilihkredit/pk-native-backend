package com.pk.app.profile.dto.response;

public record IdentityOcrLivenessCheckResponse(
        int livenessScore,
        boolean passed,
        int threshold
) {
}
