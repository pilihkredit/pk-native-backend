package com.pk.infra.profile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.profile.sync.LenderDeviceContext;
import java.util.LinkedHashMap;
import java.util.Map;

public final class DevicePayloadJsonSupport {
    private DevicePayloadJsonSupport() {
    }

    public static String toClientDeviceJson(LenderDeviceContext device, ObjectMapper objectMapper) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("appName", device.resolvedClientAppName());
        payload.put("appVersion", device.appVersion().trim());
        payload.put("packageName", device.packageName().trim());
        payload.put("deviceNo", device.deviceNo().trim());
        payload.put("systemPlatform", device.systemPlatform().trim().toLowerCase());
        putIfPresent(payload, "phoneBrand", device.resolvedExtendedAttributes().phoneBrand());
        putIfPresent(payload, "phoneBrandModel", device.resolvedExtendedAttributes().phoneBrandModel());
        putIfPresent(payload, "mac", device.resolvedExtendedAttributes().mac());
        putIfPresent(payload, "systemVersion", device.resolvedExtendedAttributes().systemVersion());
        putIfPresent(payload, "deliveryPlatform", device.resolvedExtendedAttributes().deliveryPlatform());
        putIfPresent(payload, "cpuCores", device.resolvedExtendedAttributes().cpuCores());
        putIfPresent(payload, "memoryTotal", device.resolvedExtendedAttributes().memoryTotal());
        putIfPresent(payload, "sdCardTotal", device.resolvedExtendedAttributes().sdCardTotal());
        putIfPresent(payload, "adId", device.adId());
        putIfPresent(payload, "idfv", device.resolvedExtendedAttributes().idfv());
        putIfPresent(payload, "idfa", device.resolvedExtendedAttributes().idfa());
        putIfPresent(payload, "extParam", device.resolvedExtendedAttributes().extParam());
        if (device.deviceOtherInfo() != null && !device.deviceOtherInfo().isEmpty()) {
            payload.put("deviceOtherInfo", device.deviceOtherInfo());
        }
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to serialize device payload JSON", exception);
        }
    }

    private static void putIfPresent(Map<String, Object> payload, String key, Object value) {
        if (value == null) {
            return;
        }
        if (value instanceof String stringValue && stringValue.isBlank()) {
            return;
        }
        payload.put(key, value);
    }
}
