package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * Request body for saving login log and syncing to lender.
 */
public record ProfileLoginLogSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotNull Integer loginType,
        @NotBlank @Size(max = 32) String loginIp,
        BigDecimal loginLat,
        BigDecimal loginLng,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
