package com.pk.app.profile.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for bank card list access gate.
 */
public record ProfileBankCardListAccessRequest(
        @NotNull @Valid ProfileDeviceRequest device
) {
}
