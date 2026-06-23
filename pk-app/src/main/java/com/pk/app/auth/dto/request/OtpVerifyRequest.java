package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for OTP verification and session opening.
 *
 * @param mobileNo same mobile number used in OTP send
 * @param otpToken OTP challenge token from /auth/otp/send
 * @param otpCode  one-time password received by the user
 * @param deviceNo same device identifier used in OTP send; must match X-Device-No header when the header is sent
 */
public record OtpVerifyRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String otpToken,
        @NotBlank @Size(min = 4, max = 8) String otpCode,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
