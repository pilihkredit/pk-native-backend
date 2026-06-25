package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for saving work information during onboarding.
 */
public record ProfileWorkSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotNull Integer industry,
        @NotBlank @Size(max = 128) String companyName,
        @NotBlank @Size(max = 32) String workProvinceCode,
        @NotBlank @Size(max = 32) String workCityCode,
        @NotBlank @Size(max = 32) String workDistrictCode,
        @NotBlank @Size(max = 512) String workAddress,
        @NotBlank @Size(max = 16) String income,
        @NotNull Integer payday,
        @NotNull Integer professionDegree,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
