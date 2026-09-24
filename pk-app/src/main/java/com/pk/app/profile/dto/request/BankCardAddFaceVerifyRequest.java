package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BankCardAddFaceVerifyRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 512) String livenessId,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
