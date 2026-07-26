package com.pk.core.auth;

public record UserProfileSummary(
        long userId,
        String partnerUserId,
        String mobileNo,
        boolean newlyCreated
) {
}
