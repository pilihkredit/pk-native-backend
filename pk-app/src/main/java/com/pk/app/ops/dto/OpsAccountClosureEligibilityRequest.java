package com.pk.app.ops.dto;

import jakarta.validation.constraints.Size;

public record OpsAccountClosureEligibilityRequest(
        Long userId,
        @Size(max = 32) String mobileNo,
        @Size(max = 64) String partnerUserId
) {
}
