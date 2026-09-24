package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for saving bank card during onboarding.
 */
public record ProfileBankCardSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 64) String bankCode,
        @NotBlank @Size(max = 32) String cardNumber,
        @NotNull @Valid ProfileDeviceRequest device,
        @Size(max = 64) String faceVerifyToken
) {
}
