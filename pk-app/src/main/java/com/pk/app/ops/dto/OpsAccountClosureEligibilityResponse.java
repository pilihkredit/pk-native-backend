package com.pk.app.ops.dto;

public record OpsAccountClosureEligibilityResponse(
        long userId,
        String partnerUserId,
        String mobileNo,
        boolean canClose,
        String prompt,
        String reason
) {
}
