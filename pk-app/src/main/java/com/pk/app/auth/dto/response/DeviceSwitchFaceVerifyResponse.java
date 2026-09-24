package com.pk.app.auth.dto.response;

public record DeviceSwitchFaceVerifyResponse(
        String requestId,
        boolean verified,
        String faceVerifyToken,
        long expiresIn
) {
}
