package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record IdentityOcrFaceRecognitionResponse(
        String requestId,
        double similarity,
        boolean passed,
        int threshold,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
