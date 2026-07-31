package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Personal module save result.
 *
 * @param requestId       echoed idempotency key
 * @param moduleStatus    onboarding module status; always COMPLETED on success
 * @param lenderResponse  lender {@code user/info/upsert} response {@code data} object on success
 */
public record ProfilePersonalSaveResponse(
        String requestId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
