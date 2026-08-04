package com.pk.app.profile.dto.response;

public record MobileChangeFaceVerifyResponse(
        String requestId,
        boolean verified,
        String faceVerifyToken,
        long expiresIn
) {
}
