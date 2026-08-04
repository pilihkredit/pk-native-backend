package com.pk.app.profile.dto.response;

public record MobileChangeOtpSendResponse(
        String requestId,
        String otpToken,
        long expiresIn,
        long resendAfter
) {
}
