package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for WhatsApp OTP login (no otpToken; challenge is resolved by mobile).
 *
 * @param mobileNo same mobile number used in WhatsApp send-code
 * @param otpCode  one-time password received via WhatsApp
 * @param deviceNo same device identifier used in send-code; must match X-Device-No when sent
 */
public record WhatsAppLoginRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(min = 4, max = 8) String otpCode,
        @NotBlank @Size(max = 128) String deviceNo,
        @Size(max = 64) String faceVerifyToken
) {
}
