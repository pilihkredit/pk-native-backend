package com.pk.core.profile.sync;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/** Whitelist of Feishu OpenAPI {@code deviceOtherInfo} document fields. */
public final class DeviceOtherInfoDocumentFields {
    public static final Set<String> NAMES = Set.of(
            "allowMockLocation",
            "appSign",
            "basebandVersion",
            "battery",
            "batteryTemp",
            "brand",
            "brightness",
            "cameraNum",
            "cpuHardware",
            "dbm",
            "diskFreeSpace",
            "diskSpace",
            "fingerPrint",
            "freeMemory",
            "gaid",
            "imsi",
            "isAcCharge",
            "isCharging",
            "isDebug",
            "isNetworkingRoaming",
            "isProxy",
            "isRoot",
            "isSimulator",
            "isUsbCharge",
            "kernelVersion",
            "keyboard",
            "lastBootTime",
            "localDisplayLanguage",
            "localIso3Country",
            "localIso3Language",
            "macAddress",
            "manufacturer",
            "modelNo",
            "networkCountryIso",
            "networkOperator",
            "networkType",
            "osVersion",
            "screenHeight",
            "screenWidth",
            "sensor",
            "serialNo",
            "simCountryIso",
            "simOperator",
            "simOperatorName",
            "timezone",
            "totalMemory",
            "upTime",
            "wifiBSSID",
            "wifiRSSI",
            "wifiSSID",
            "mac",
            "netMode",
            "memoryTotal",
            "memoryAvailable",
            "sdCardTotal",
            "sdCardAvailable",
            "upTimeSec",
            "displayMetrics",
            "cpuCores",
            "cpuName",
            "cpuMaxFreq",
            "cpuMinFreq",
            "cpuCurFreq"
    );

    private DeviceOtherInfoDocumentFields() {
    }

    public static Map<String, Object> filter(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> filtered = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            if (entry.getKey() == null || !NAMES.contains(entry.getKey())) {
                continue;
            }
            Object value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (value instanceof String stringValue && stringValue.isBlank()) {
                continue;
            }
            filtered.put(entry.getKey(), value);
        }
        return filtered.isEmpty() ? Map.of() : Collections.unmodifiableMap(filtered);
    }
}
