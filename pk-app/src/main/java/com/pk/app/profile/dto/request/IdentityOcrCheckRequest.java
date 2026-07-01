package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;

public record IdentityOcrCheckRequest(
        @NotBlank String imageBase64
) {
}
