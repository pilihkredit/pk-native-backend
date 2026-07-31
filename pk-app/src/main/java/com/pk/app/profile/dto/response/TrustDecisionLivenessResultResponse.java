package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record TrustDecisionLivenessResultResponse(
        String requestId,
        String result,
        String sequenceId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
