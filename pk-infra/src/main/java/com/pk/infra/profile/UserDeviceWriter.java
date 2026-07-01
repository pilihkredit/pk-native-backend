package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.sync.LenderDeviceContext;
import org.springframework.stereotype.Component;

@Component
public class UserDeviceWriter {
    private final ProfileDeviceRepository profileDeviceRepository;
    private final ObjectMapper objectMapper;

    public UserDeviceWriter(ProfileDeviceRepository profileDeviceRepository, ObjectMapper objectMapper) {
        this.profileDeviceRepository = profileDeviceRepository;
        this.objectMapper = objectMapper;
    }

    public void upsertFromRequest(
            long profileId,
            String partnerUserId,
            String requestId,
            LenderDeviceContext device
    ) {
        String deviceJson = DevicePayloadJsonSupport.toClientDeviceJson(device, objectMapper);
        profileDeviceRepository.upsertByDeviceNo(new ProfileDeviceData(
                profileId,
                partnerUserId.trim(),
                device.deviceNo().trim(),
                device.systemPlatform().trim().toLowerCase(),
                device.resolvedClientAppName(),
                device.appVersion().trim(),
                device.packageName().trim(),
                normalizeOptional(device.adId()),
                deviceJson,
                requestId.trim()
        ));
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
