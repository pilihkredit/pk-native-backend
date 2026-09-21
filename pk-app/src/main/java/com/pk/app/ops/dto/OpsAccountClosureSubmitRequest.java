package com.pk.app.ops.dto;

import jakarta.validation.constraints.Size;

public record OpsAccountClosureSubmitRequest(
        Long userId,
        @Size(max = 32) String mobileNo,
        @Size(max = 64) String partnerUserId,
        @Size(min = 1, max = 200) String reason,
        @Size(max = 64) String operatorId
) {
}
