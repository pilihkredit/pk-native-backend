package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record IdentityOcrDevLenderSyncResponse(
        String requestId,
        JsonNode lenderResponse
) {
}
