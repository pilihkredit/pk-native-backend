package com.pk.app.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReviewGuideFeedbackRequest(
        @NotNull @Positive Long guideId,
        @NotNull @Min(1) @Max(5) Integer rating
) {
}
