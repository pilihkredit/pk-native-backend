package com.pk.app.push.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PushDeviceRegisterRequest(
        @NotBlank @Size(max = 512) String fcmToken,
        @NotBlank @Pattern(regexp = "GRANTED|DENIED|NOT_DETERMINED") String permissionStatus
) {
}
