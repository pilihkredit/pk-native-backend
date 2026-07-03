package com.pk.app.home.dto.request;

import com.pk.app.profile.dto.request.ProfileDeviceRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record HomeSummaryRequest(
        @NotNull @Valid ProfileDeviceRequest device
) {
}
