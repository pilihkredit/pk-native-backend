package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MobileChangeFaceVerifyRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 512) String livenessId,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
