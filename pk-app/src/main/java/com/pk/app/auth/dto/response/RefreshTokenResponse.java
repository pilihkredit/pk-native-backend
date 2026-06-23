package com.pk.app.auth.dto.response;

/**
 * New access token from a valid refresh token. Refresh token itself is not rotated.
 *
 * @param accessToken new JWT access token
 * @param tokenType   token type for Authorization header, usually Bearer
 * @param expiresIn   access token lifetime in seconds
 */
public record RefreshTokenResponse(String accessToken, String tokenType, long expiresIn) {
}
