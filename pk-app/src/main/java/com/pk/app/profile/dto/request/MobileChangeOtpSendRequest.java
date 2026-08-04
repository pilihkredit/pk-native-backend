package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MobileChangeOtpSendRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 32) String newMobileNo,
        @NotBlank @Size(max = 64) String faceVerifyToken,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
