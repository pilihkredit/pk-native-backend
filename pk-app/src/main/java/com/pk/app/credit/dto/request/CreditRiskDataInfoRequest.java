package com.pk.app.credit.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreditRiskDataInfoRequest(
        @NotNull @Valid com.pk.app.profile.dto.request.ProfileDeviceRequest openUserDevice,
        @NotNull List<@Valid CreditAppInfoRequest> appList
) {
}
