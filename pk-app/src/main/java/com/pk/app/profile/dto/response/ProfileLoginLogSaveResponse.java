package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Login log module save result.
 */
public record ProfileLoginLogSaveResponse(
        String requestId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
