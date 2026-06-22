package com.pk.app.api.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MobileCheckRequest(
        @NotBlank @Size(max = 32) String mobileNo,
        @NotBlank @Size(max = 128) String deviceNo
) {
}
