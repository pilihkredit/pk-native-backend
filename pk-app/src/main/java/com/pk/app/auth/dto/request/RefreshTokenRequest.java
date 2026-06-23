package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body for refreshing an access token.
 *
 * @param refreshToken long-lived refresh token from OTP verify; not rotated on refresh
 */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
