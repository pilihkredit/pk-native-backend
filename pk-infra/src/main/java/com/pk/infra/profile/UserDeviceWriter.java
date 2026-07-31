package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.UserDeviceOtherInfoData;
import com.pk.core.profile.port.ProfileDeviceRepository;
import com.pk.core.profile.port.UserDeviceOtherInfoRepository;
import com.pk.core.profile.sync.DeviceExtendedAttributes;
import com.pk.core.profile.sync.LenderDeviceContext;
import com.pk.core.profile.sync.LenderDevicePayloadBuilder;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class UserDeviceWriter {
    private final ProfileDeviceRepository profileDeviceRepository;
    private final UserDeviceOtherInfoRepository userDeviceOtherInfoRepository;
    private final ObjectMapper objectMapper;

    public UserDeviceWriter(
            ProfileDeviceRepository profileDeviceRepository,
            UserDeviceOtherInfoRepository userDeviceOtherInfoRepository,
            ObjectMapper objectMapper
    ) {
        this.profileDeviceRepository = profileDeviceRepository;
        this.userDeviceOtherInfoRepository = userDeviceOtherInfoRepository;
        this.objectMapper = objectMapper;
    }

    public void upsertFromRequest(
            long userId,
            String partnerUserId,
            String requestId,
            LenderDeviceContext device
    ) {
        Map<String, Object> payload = LenderDevicePayloadBuilder.buildProfileSyncDevice(device);
        String deviceJson;
        try {
            deviceJson = objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize lender device JSON", exception);
        }
        DeviceExtendedAttributes attrs = device.resolvedExtendedAttributes();
        profileDeviceRepository.upsertByDeviceNo(new ProfileDeviceData(
                userId,
                partnerUserId.trim(),
                device.deviceNo().trim(),
                device.systemPlatform().trim().toLowerCase(),
                device.appName().trim(),
                device.appVersion().trim(),
                device.packageName().trim(),
                normalizeOptional(attrs.phoneBrand()),
                normalizeOptional(attrs.phoneBrandModel()),
                normalizeOptional(attrs.mac()),
                normalizeOptional(attrs.systemVersion()),
                normalizeOptional(attrs.deliveryPlatform()),
                attrs.cpuCores(),
                attrs.memoryTotal(),
                attrs.sdCardTotal(),
                normalizeOptional(device.adId()),
                normalizeOptional(attrs.idfv()),
                normalizeOptional(attrs.idfa()),
                normalizeOptional(attrs.extParam()),
                normalizeOptional(attrs.ip()),
                deviceJson,
                requestId.trim()
        ));

        Object otherObj = payload.get("deviceOtherInfo");
        String deviceNo = device.deviceNo().trim();
        if (otherObj instanceof Map<?, ?> map && !map.isEmpty()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> filtered = (Map<String, Object>) map;
            String otherJson;
            try {
                otherJson = objectMapper.writeValueAsString(filtered);
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to serialize deviceOtherInfo JSON", exception);
            }
            userDeviceOtherInfoRepository.upsert(UserDeviceOtherInfoData.fromFilteredMap(
                    userId,
                    partnerUserId.trim(),
                    deviceNo,
                    filtered,
                    otherJson
            ));
        } else {
            userDeviceOtherInfoRepository.deleteByDeviceNo(deviceNo);
        }
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
