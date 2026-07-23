package com.pk.core.profile.sync;

import java.util.LinkedHashMap;
import java.util.Map;

/** Builds lender {@code userInfo.device} / {@code openUserDevice} shaped maps (no Jackson). */
public final class LenderDevicePayloadBuilder {
    /** Temporary fixed client IP until frontend/header IP is wired through. */
    public static final String FIXED_CLIENT_IP = "147.139.188.108";

    private LenderDevicePayloadBuilder() {
    }

    public static Map<String, Object> buildProfileSyncDevice(LenderDeviceContext device) {
        return build(device, false);
    }

    public static Map<String, Object> buildRiskApplyDevice(LenderDeviceContext device) {
        return build(device, true);
    }

    private static Map<String, Object> build(LenderDeviceContext device, boolean requireDeviceOtherInfo) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("appName", device.appName());
        payload.put("appVersion", device.appVersion());
        payload.put("packageName", device.packageName());
        payload.put("deviceNo", device.deviceNo());
        payload.put("systemPlatform", device.systemPlatform());
        applyExtendedAttributes(payload, device.resolvedExtendedAttributes());
        putIfPresent(payload, "adId", device.adId());
        putIfPresent(payload, "adChannel", device.resolvedExtendedAttributes().adChannel());
        payload.put("ip", FIXED_CLIENT_IP);
        Map<String, Object> filteredOther = DeviceOtherInfoDocumentFields.filter(device.deviceOtherInfo());
        if (requireDeviceOtherInfo) {
            payload.put("deviceOtherInfo", filteredOther.isEmpty() ? new LinkedHashMap<>() : new LinkedHashMap<>(filteredOther));
        } else if (!filteredOther.isEmpty()) {
            payload.put("deviceOtherInfo", new LinkedHashMap<>(filteredOther));
        }
        return payload;
    }

    private static void applyExtendedAttributes(Map<String, Object> payload, DeviceExtendedAttributes attributes) {
        putIfPresent(payload, "phoneBrand", attributes.phoneBrand());
        putIfPresent(payload, "phoneBrandModel", attributes.phoneBrandModel());
        putIfPresent(payload, "mac", attributes.mac());
        putIfPresent(payload, "systemVersion", attributes.systemVersion());
        putIfPresent(payload, "deliveryPlatform", attributes.deliveryPlatform());
        if (attributes.cpuCores() != null) {
            payload.put("cpuCores", attributes.cpuCores());
        }
        if (attributes.memoryTotal() != null) {
            payload.put("memoryTotal", attributes.memoryTotal());
        }
        if (attributes.sdCardTotal() != null) {
            payload.put("sdCardTotal", attributes.sdCardTotal());
        }
        putIfPresent(payload, "idfv", attributes.idfv());
        putIfPresent(payload, "idfa", attributes.idfa());
        putIfPresent(payload, "extParam", attributes.extParam());
    }

    private static void putIfPresent(Map<String, Object> payload, String field, String value) {
        if (value != null && !value.isBlank()) {
            payload.put(field, value.trim());
        }
    }
}
