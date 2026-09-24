package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceSwitchFaceVerifyRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 512) String livenessId,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
