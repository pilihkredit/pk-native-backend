package com.pk.app.ops.dto;

public record OpsAccountClosureSubmitResponse(
        long userId,
        String partnerUserId,
        String mobileNo,
        String queueStatus
) {
}
