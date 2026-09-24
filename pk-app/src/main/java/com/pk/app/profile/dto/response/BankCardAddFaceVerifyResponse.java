package com.pk.app.profile.dto.response;

public record BankCardAddFaceVerifyResponse(
        String requestId,
        boolean verified,
        String faceVerifyToken,
        long expiresIn
) {
}
