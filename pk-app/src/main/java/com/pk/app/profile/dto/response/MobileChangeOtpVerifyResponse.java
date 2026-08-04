package com.pk.app.profile.dto.response;

public record MobileChangeOtpVerifyResponse(
        String requestId,
        boolean changed,
        String mobileNo,
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn
) {
}
