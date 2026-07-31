package com.pk.app.callback.dto.response;

public record CallbackOAuthTokenResponse(
        String accessToken,
        long expiresIn
) {
}
