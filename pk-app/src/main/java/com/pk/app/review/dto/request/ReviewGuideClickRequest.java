package com.pk.app.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record ReviewGuideClickRequest(
        @NotNull @Positive Long guideId,
        @NotBlank @Pattern(regexp = "FAKE_CLICK|RATE") String action
) {
}
