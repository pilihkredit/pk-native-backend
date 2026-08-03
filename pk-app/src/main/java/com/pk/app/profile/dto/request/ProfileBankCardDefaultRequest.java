package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ProfileBankCardDefaultRequest(
        @NotBlank @Size(max = 64) String requestId,
        @Positive long bankCardId
) {
}
