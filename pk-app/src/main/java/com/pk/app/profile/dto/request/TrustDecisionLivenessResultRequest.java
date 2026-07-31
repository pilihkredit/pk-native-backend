package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TrustDecisionLivenessResultRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 512) String livenessId,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
