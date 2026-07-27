package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record TrustDecisionFaceRecognitionResponse(
        String requestId,
        String result,
        double similarity,
        String sequenceId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
