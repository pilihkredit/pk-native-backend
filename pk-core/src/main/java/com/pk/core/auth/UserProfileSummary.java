package com.pk.core.auth;

public record UserProfileSummary(
        long profileId,
        String partnerUserId,
        String mobileNo,
        boolean newlyCreated
) {
}
