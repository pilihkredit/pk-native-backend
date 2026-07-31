package com.pk.app.profile.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Manual legal name + EKTP before OCR face recognition / lender identity sync.
 * Local persistence only — no lender call, no device payload.
 */
public record IdentityBasicSaveRequest(
        @NotBlank @Size(max = 64) String requestId,
        @NotBlank @Size(max = 128) String name,
        @NotBlank @Size(min = 16, max = 16) String idNo
) {
}
