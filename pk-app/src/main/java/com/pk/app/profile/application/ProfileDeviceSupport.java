package com.pk.app.profile.application;

import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.app.profile.dto.request.ProfileDeviceRequest;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;

public final class ProfileDeviceSupport {
    private ProfileDeviceSupport() {
    }

    public static LenderDeviceContext resolveLenderDevice(
            ProfileDeviceRequest deviceRequest,
            ClientRequestHeaders.ResolvedClientHeaders headers,
            PendanaanProperties pendanaanProperties
    ) {
        return resolveLenderDevice(deviceRequest, headers, pendanaanProperties, false);
    }

    public static LenderDeviceContext resolveRiskLenderDevice(
            ProfileDeviceRequest deviceRequest,
            ClientRequestHeaders.ResolvedClientHeaders headers,
            PendanaanProperties pendanaanProperties
    ) {
        return resolveLenderDevice(deviceRequest, headers, pendanaanProperties, true);
    }

    private static LenderDeviceContext resolveLenderDevice(
            ProfileDeviceRequest deviceRequest,
            ClientRequestHeaders.ResolvedClientHeaders headers,
            PendanaanProperties pendanaanProperties,
            boolean requireDeviceOtherInfo
    ) {
        validateAgainstHeaders(deviceRequest, headers);
        if (requireDeviceOtherInfo && deviceRequest.deviceOtherInfo() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device.deviceOtherInfo is required");
        }
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
                deviceRequest.appName().trim(),
                toExtendedAttributes(deviceRequest)
        );
    }

    private static DeviceExtendedAttributes toExtendedAttributes(ProfileDeviceRequest deviceRequest) {
        return new DeviceExtendedAttributes(
                normalizeOptional(deviceRequest.phoneBrand()),
                normalizeOptional(deviceRequest.phoneBrandModel()),
                normalizeOptional(deviceRequest.mac()),
                normalizeOptional(deviceRequest.systemVersion()),
                normalizeOptional(deviceRequest.deliveryPlatform()),
                deviceRequest.cpuCores(),
                deviceRequest.memoryTotal(),
                deviceRequest.sdCardTotal(),
                normalizeOptional(deviceRequest.idfv()),
                normalizeOptional(deviceRequest.idfa()),
                normalizeOptional(deviceRequest.extParam())
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
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device." + fieldName + " or matching header is missing"
            );
        }
        if ("systemPlatform".equals(fieldName)) {
            if (!bodyValue.trim().equalsIgnoreCase(headerValue.trim())) {
                throw new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.systemPlatform does not match header X-Platform"
                );
            }
            return;
        }
        if ("deviceNo".equals(fieldName)) {
            if (!bodyValue.trim().equals(headerValue.trim())) {
                throw new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.deviceNo does not match header X-Device-No"
                );
            }
            return;
        }
        if ("appVersion".equals(fieldName)) {
            if (!bodyValue.trim().equals(headerValue.trim())) {
                throw new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.appVersion does not match header X-App-Version"
                );
            }
            return;
        }
        if ("packageName".equals(fieldName)) {
            if (!bodyValue.trim().equals(headerValue.trim())) {
                throw new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.packageName does not match header X-App-Package"
                );
            }
            return;
        }
        if (!bodyValue.trim().equals(headerValue.trim())) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device." + fieldName + " does not match request header"
            );
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
