package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record ProfileAppsFlyerInstallSaveResponse(
        String requestId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
