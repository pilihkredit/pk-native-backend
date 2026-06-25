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
        @NotBlank @Size(max = 32) String provinceCode,
        @NotBlank @Size(max = 32) String cityCode,
        @NotBlank @Size(max = 32) String districtCode,
        @NotBlank @Size(max = 512) String address,
        @NotNull Integer educationDegree,
        @NotBlank @Size(max = 128) String motherSurname,
        @Size(max = 128) String userEmail,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
