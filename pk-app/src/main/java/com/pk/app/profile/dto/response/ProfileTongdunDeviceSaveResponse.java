package com.pk.app.profile.dto.response;

import com.fasterxml.jackson.databind.JsonNode;

public record ProfileTongdunDeviceSaveResponse(
        String requestId,
        String moduleStatus,
        JsonNode lenderResponse
) {
}
