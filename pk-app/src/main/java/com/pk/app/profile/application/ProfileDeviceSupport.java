package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.dto.request.ProfileDeviceRequest;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.LenderDeviceContext;

public final class ProfileDeviceSupport {
    private ProfileDeviceSupport() {
    }

    public static LenderDeviceContext resolveLenderDevice(
            ProfileDeviceRequest deviceRequest,
            ClientRequestHeaders.ResolvedClientHeaders headers,
            PendanaanProperties pendanaanProperties
    ) {
        validateAgainstHeaders(deviceRequest, headers);
        String lenderAppName = pendanaanProperties.appName();
        if (lenderAppName == null || lenderAppName.isBlank()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        return new LenderDeviceContext(
                lenderAppName.trim(),
                deviceRequest.appVersion().trim(),
                deviceRequest.packageName().trim(),
                deviceRequest.deviceNo().trim(),
                deviceRequest.systemPlatform().trim().toLowerCase(),
                normalizeOptional(deviceRequest.adId()),
                deviceRequest.deviceOtherInfo(),
                deviceRequest.appName().trim()
        );
    }

    private static void validateAgainstHeaders(
            ProfileDeviceRequest deviceRequest,
            ClientRequestHeaders.ResolvedClientHeaders headers
    ) {
        requireMatch("deviceNo", deviceRequest.deviceNo(), headers.deviceNo());
        requireMatch("appVersion", deviceRequest.appVersion(), headers.appVersion());
        requireMatch("packageName", deviceRequest.packageName(), headers.appPackage());
        requireMatch("systemPlatform", deviceRequest.systemPlatform(), headers.platform());
    }

    private static void requireMatch(String fieldName, String bodyValue, String headerValue) {
        if (bodyValue == null || headerValue == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        if ("systemPlatform".equals(fieldName)) {
            if (!bodyValue.trim().equalsIgnoreCase(headerValue.trim())) {
                throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
            }
            return;
        }
        if (!bodyValue.trim().equals(headerValue.trim())) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
