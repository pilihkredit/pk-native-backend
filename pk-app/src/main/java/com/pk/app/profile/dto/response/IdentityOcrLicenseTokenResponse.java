package com.pk.app.profile.dto.response;

public record IdentityOcrLicenseTokenResponse(
        String licenseToken,
        long effectiveSeconds
) {
}
