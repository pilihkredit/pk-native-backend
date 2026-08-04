package com.pk.app.review.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ReviewGuideClaimRequest(
        @NotBlank
        @Pattern(regexp = "CREDIT_FAILED|ORDER_CREATED|LOAN_PAID") String scene
) {
}
