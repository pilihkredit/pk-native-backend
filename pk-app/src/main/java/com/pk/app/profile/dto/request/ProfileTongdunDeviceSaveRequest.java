package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Tongdun device fingerprint payload; syncs lender {@code userInfo.tongdunDevice}.
 */
public record ProfileTongdunDeviceSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 16) String sceneType,
        @NotBlank @Size(max = 256) String tongdunKey,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
