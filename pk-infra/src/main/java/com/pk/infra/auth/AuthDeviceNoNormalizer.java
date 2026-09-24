package com.pk.infra.auth;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;

public final class AuthDeviceNoNormalizer {
    private AuthDeviceNoNormalizer() {
    }

    public static String normalize(String deviceNo) {
        return deviceNo == null ? null : deviceNo.trim();
    }

    public static String requireNonBlank(String deviceNo) {
        String normalized = normalize(deviceNo);
        if (normalized == null || normalized.isEmpty()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return normalized;
    }
}
