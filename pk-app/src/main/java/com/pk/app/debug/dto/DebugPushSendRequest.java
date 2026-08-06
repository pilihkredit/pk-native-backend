package com.pk.app.debug.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;

public record DebugPushSendRequest(
        @NotBlank String fcmToken,
        String title,
        String body,
        Map<String, String> data
) {
}
