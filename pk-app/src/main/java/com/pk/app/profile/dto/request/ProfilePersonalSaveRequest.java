package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for saving personal basic information during onboarding.
 */
public record ProfilePersonalSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotNull @Valid ProfilePersonalInfoRequest profile,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
