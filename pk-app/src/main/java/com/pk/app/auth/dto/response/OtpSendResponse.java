package com.pk.app.auth.dto.response;

/**
 * OTP send result.
 *
 * @param otpToken    opaque token for /auth/otp/verify
 * @param expireIn    OTP validity window in seconds
 * @param resendAfter minimum seconds before the same device can request another OTP
 */
public record OtpSendResponse(String otpToken, long expireIn, long resendAfter) {
}
