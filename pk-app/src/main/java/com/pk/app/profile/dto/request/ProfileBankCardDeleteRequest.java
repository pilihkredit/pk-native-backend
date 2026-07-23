package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request body for soft-deleting a bank card.
 */
public record ProfileBankCardDeleteRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 32) String cardNumber,
        @NotNull @Valid ProfileDeviceRequest device
) {
}
