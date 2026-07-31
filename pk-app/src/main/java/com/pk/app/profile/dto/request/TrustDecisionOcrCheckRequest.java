package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrustDecisionOcrCheckRequest(@NotBlank String imageBase64) {
}
