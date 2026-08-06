package com.pk.app.debug.application;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugPushSendRequest;
import com.pk.app.debug.dto.DebugPushSendResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.port.FcmPushPort;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class DebugPushSendApplicationService {
    private final DebugUserProgressProperties properties;
    private final FcmPushPort fcmPushPort;

    public DebugPushSendApplicationService(
            DebugUserProgressProperties properties,
            FcmPushPort fcmPushPort
    ) {
        this.properties = properties;
        this.fcmPushPort = fcmPushPort;
    }

    public DebugPushSendResponse send(String debugToken, DebugPushSendRequest request) {
        validateToken(debugToken);
        if (request == null || request.fcmToken() == null || request.fcmToken().isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        Map<String, String> data = normalizeData(request.data());
        FcmPushPort.FcmSendResult result = fcmPushPort.send(new FcmPushPort.FcmSendCommand(
                request.fcmToken().trim(),
                blankToNull(request.title()),
                blankToNull(request.body()),
                data
        ));
        return new DebugPushSendResponse(result.success(), result.messageName(), result.rawBody());
    }

    private void validateToken(String debugToken) {
        if (!properties.enabled() || !properties.tokenConfigured()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if (debugToken == null || !properties.token().equals(debugToken.trim())) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private static Map<String, String> normalizeData(Map<String, String> data) {
        if (data == null || data.isEmpty()) {
            return Map.of();
        }
        Map<String, String> normalized = new LinkedHashMap<>();
        data.forEach((key, value) -> {
            if (key != null && !key.isBlank() && value != null) {
                normalized.put(key.trim(), value);
            }
        });
        return normalized;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
