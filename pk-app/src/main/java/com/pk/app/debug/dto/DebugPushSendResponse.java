package com.pk.app.debug.dto;

public record DebugPushSendResponse(
        boolean success,
        String messageName,
        String rawBody
) {
}
