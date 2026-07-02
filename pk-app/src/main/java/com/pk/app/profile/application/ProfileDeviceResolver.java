package com.pk.app.profile.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.adapter.pendanaan.PendanaanProperties;
import com.pk.app.common.web.ClientRequestHeaders;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.AuthenticatedPrincipal;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.infra.profile.StoredDevicePayloadReader;
import org.springframework.stereotype.Component;

@Component
public class ProfileDeviceResolver {
    private final ProfileDeviceRepository profileDeviceRepository;
    private final PendanaanProperties pendanaanProperties;
    private final ObjectMapper objectMapper;

    public ProfileDeviceResolver(
            ProfileDeviceRepository profileDeviceRepository,
            PendanaanProperties pendanaanProperties,
            ObjectMapper objectMapper
    ) {
        this.profileDeviceRepository = profileDeviceRepository;
        this.pendanaanProperties = pendanaanProperties;
        this.objectMapper = objectMapper;
    }

    public LenderDeviceContext resolve(AuthenticatedPrincipal principal, ClientRequestHeaders.ResolvedClientHeaders headers) {
        if (principal == null) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
        var stored = profileDeviceRepository.findByDeviceNo(headers.deviceNo())
                .orElseThrow(() -> new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device record not found for current device"
                ));
        if (stored.profileId() != principal.profileId()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device does not belong to current user");
        }
        validateAgainstHeaders(stored, headers);
        return StoredDevicePayloadReader.toLenderDevice(stored, pendanaanProperties.appName(), objectMapper);
    }

    private static void validateAgainstHeaders(
            com.pk.core.profile.ProfileDeviceData stored,
            ClientRequestHeaders.ResolvedClientHeaders headers
    ) {
        requireMatch("deviceNo", stored.deviceNo(), headers.deviceNo());
        requireMatch("appVersion", stored.appVersion(), headers.appVersion());
        requireMatch("packageName", stored.packageName(), headers.appPackage());
        requireMatch("systemPlatform", stored.systemPlatform(), headers.platform());
    }

    private static void requireMatch(String fieldName, String storedValue, String headerValue) {
        if (storedValue == null || headerValue == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS, "device." + fieldName + " is missing");
        }
        if ("systemPlatform".equals(fieldName)) {
            if (!storedValue.trim().equalsIgnoreCase(headerValue.trim())) {
                throw new ApiException(
                        ApiCode.INVALID_REQUEST_PARAMETERS,
                        "device.systemPlatform does not match header X-Platform"
                );
            }
            return;
        }
        if (!storedValue.trim().equals(headerValue.trim())) {
            throw new ApiException(
                    ApiCode.INVALID_REQUEST_PARAMETERS,
                    "device." + fieldName + " does not match request header"
            );
        }
    }
}
