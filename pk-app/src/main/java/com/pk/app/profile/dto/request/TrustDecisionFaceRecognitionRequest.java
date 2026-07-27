package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrustDecisionFaceRecognitionRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank String faceImageBase64,
        @NotBlank String idCardImageBase64,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
