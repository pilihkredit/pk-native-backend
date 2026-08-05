package com.pk.app.launch.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record AppLaunchRecordRequest(
        @NotBlank @Size(max = 64) String launchId,
        @Positive Long clientStartedAt,
        @Size(max = 128) String idfv
) {
}
