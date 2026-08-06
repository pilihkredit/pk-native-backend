package com.pk.app.auth.dto.response;

/** Account closure eligibility result with optional block copy for UI. */
public record AccountCloseEligibilityResponse(boolean canClose, String prompt, String reason) {
}
