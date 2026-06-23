package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProfileContactItemRequest(
        @NotNull Integer relationship,
        @NotBlank @Size(max = 128) String contactName,
        @NotBlank @Size(max = 32) String contactMobile
) {
}
