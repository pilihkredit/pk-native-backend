package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileMobileChangeRequest(
        @NotBlank @Size(max = 32) String newMobileNo
) {
}
