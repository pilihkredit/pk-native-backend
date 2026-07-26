package com.pk.core.profile;

import java.util.Map;

public record UserDeviceOtherInfoData(
        long userId,
        String partnerUserId,
        String deviceNo,
        Boolean allowMockLocation,
        String appSign,
        String basebandVersion,
        String battery,
        Integer batteryTemp,
        String brand,
        Integer brightness,
        String cameraNum,
        String cpuHardware,
        Integer dbm,
        String diskFreeSpace,
        String diskSpace,
        String fingerPrint,
        String freeMemory,
        String gaid,
        String imsi,
        Boolean isAcCharge,
        Boolean isCharging,
        Boolean isDebug,
        String isNetworkingRoaming,
        Boolean isProxy,
        Boolean isRoot,
        Boolean isSimulator,
        Boolean isUsbCharge,
        String kernelVersion,
        Integer keyboard,
        Long lastBootTime,
        String localDisplayLanguage,
        String localIso3Country,
        String localIso3Language,
        String macAddress,
        String manufacturer,
        String modelNo,
        String networkCountryIso,
        String networkOperator,
        String networkType,
        String osVersion,
        String screenHeight,
        String screenWidth,
        String sensor,
        String serialNo,
        String simCountryIso,
        String simOperator,
        String simOperatorName,
        String timezone,
        String totalMemory,
        String upTime,
        String wifiBSSID,
        String wifiRSSI,
        String wifiSSID,
        String mac,
        Integer netMode,
        Long memoryTotal,
        Long memoryAvailable,
        Long sdCardTotal,
        Long sdCardAvailable,
        Long upTimeSec,
        String displayMetrics,
        Integer cpuCores,
        String cpuName,
        Long cpuMaxFreq,
        Long cpuMinFreq,
        Long cpuCurFreq,
        String deviceOtherInfoJson
) {
    public static UserDeviceOtherInfoData fromFilteredMap(
            long userId,
            String partnerUserId,
            String deviceNo,
            Map<String, Object> filtered,
            String deviceOtherInfoJson
    ) {
        Map<String, Object> src = filtered == null ? Map.of() : filtered;
        return new UserDeviceOtherInfoData(
                userId,
                partnerUserId,
                deviceNo,
                asBoolean(src.get("allowMockLocation")),
                asString(src.get("appSign")),
                asString(src.get("basebandVersion")),
                asString(src.get("battery")),
                asInteger(src.get("batteryTemp")),
                asString(src.get("brand")),
                asInteger(src.get("brightness")),
                asString(src.get("cameraNum")),
                asString(src.get("cpuHardware")),
                asInteger(src.get("dbm")),
                asString(src.get("diskFreeSpace")),
                asString(src.get("diskSpace")),
                asString(src.get("fingerPrint")),
                asString(src.get("freeMemory")),
                asString(src.get("gaid")),
                asString(src.get("imsi")),
                asBoolean(src.get("isAcCharge")),
                asBoolean(src.get("isCharging")),
                asBoolean(src.get("isDebug")),
                asString(src.get("isNetworkingRoaming")),
                asBoolean(src.get("isProxy")),
                asBoolean(src.get("isRoot")),
                asBoolean(src.get("isSimulator")),
                asBoolean(src.get("isUsbCharge")),
                asString(src.get("kernelVersion")),
                asInteger(src.get("keyboard")),
                asLong(src.get("lastBootTime")),
                asString(src.get("localDisplayLanguage")),
                asString(src.get("localIso3Country")),
                asString(src.get("localIso3Language")),
                asString(src.get("macAddress")),
                asString(src.get("manufacturer")),
                asString(src.get("modelNo")),
                asString(src.get("networkCountryIso")),
                asString(src.get("networkOperator")),
                asString(src.get("networkType")),
                asString(src.get("osVersion")),
                asString(src.get("screenHeight")),
                asString(src.get("screenWidth")),
                asString(src.get("sensor")),
                asString(src.get("serialNo")),
                asString(src.get("simCountryIso")),
                asString(src.get("simOperator")),
                asString(src.get("simOperatorName")),
                asString(src.get("timezone")),
                asString(src.get("totalMemory")),
                asString(src.get("upTime")),
                asString(src.get("wifiBSSID")),
                asString(src.get("wifiRSSI")),
                asString(src.get("wifiSSID")),
                asString(src.get("mac")),
                asInteger(src.get("netMode")),
                asLong(src.get("memoryTotal")),
                asLong(src.get("memoryAvailable")),
                asLong(src.get("sdCardTotal")),
                asLong(src.get("sdCardAvailable")),
                asLong(src.get("upTimeSec")),
                asString(src.get("displayMetrics")),
                asInteger(src.get("cpuCores")),
                asString(src.get("cpuName")),
                asLong(src.get("cpuMaxFreq")),
                asLong(src.get("cpuMinFreq")),
                asLong(src.get("cpuCurFreq")),
                deviceOtherInfoJson
        );
    }

    private static String asString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static Boolean asBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean bool) {
            return bool;
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Boolean.parseBoolean(text);
    }

    private static Integer asInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Integer.valueOf(text);
    }

    private static Long asLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        String text = String.valueOf(value).trim();
        if (text.isEmpty()) {
            return null;
        }
        return Long.valueOf(text);
    }
}
