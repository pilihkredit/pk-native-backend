package com.pk.app.web;

import com.pk.core.auth.UserStage;

public record AuthenticatedUser(
        long profileId,
        String partnerUserId,
        String mobileNo,
        String kycStatus
) {
    public UserStage resolveUserStage() {
        if ("INCOMPLETE".equals(kycStatus) || "READY".equals(kycStatus)) {
            return UserStage.ONBOARDING;
        }
        if ("SYNCED".equals(kycStatus)) {
            return UserStage.CREDIT_PENDING;
        }
        return UserStage.ONBOARDING;
    }
}
