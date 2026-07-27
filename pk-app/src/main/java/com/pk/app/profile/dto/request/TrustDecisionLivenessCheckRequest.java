package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;

public record TrustDecisionLivenessCheckRequest(@NotBlank String imageBase64) {
}
