package com.pk.app.common.web;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import jakarta.servlet.http.HttpServletRequest;

public final class ClientRequestHeaders {
    private ClientRequestHeaders() {
    }

    public static ResolvedClientHeaders require(HttpServletRequest request) {
        String deviceNo = trimToNull(request.getHeader("X-Device-No"));
        String appVersion = trimToNull(request.getHeader("X-App-Version"));
        String platform = trimToNull(request.getHeader("X-Platform"));
        String appPackage = trimToNull(request.getHeader("X-App-Package"));
        if (deviceNo == null) {
            throw new ApiException(ApiCode.DEVICE_NO_REQUIRED);
        }
        if (appVersion == null || platform == null || appPackage == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return new ResolvedClientHeaders(deviceNo, appVersion, platform, appPackage);
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public record ResolvedClientHeaders(
            String deviceNo,
            String appVersion,
            String platform,
            String appPackage
    ) {
    }
}
