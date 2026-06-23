package com.pk.core.auth;

public record TokenPair(
        String accessToken,
        String refreshToken,
        long accessTokenExpiresInSeconds,
        String tokenType
) {
    public TokenPair {
        if (tokenType == null || tokenType.isBlank()) {
            tokenType = "Bearer";
        }
    }

    public static TokenPair of(String accessToken, String refreshToken, long accessTokenExpiresInSeconds) {
        return new TokenPair(accessToken, refreshToken, accessTokenExpiresInSeconds, "Bearer");
    }
}
