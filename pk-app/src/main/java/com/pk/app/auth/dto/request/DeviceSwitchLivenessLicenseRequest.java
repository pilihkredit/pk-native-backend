package com.pk.app.auth.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DeviceSwitchLivenessLicenseRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String deviceNo,
        @Min(1) @Max(86_400) Integer sessionDurationSeconds
) {
}
