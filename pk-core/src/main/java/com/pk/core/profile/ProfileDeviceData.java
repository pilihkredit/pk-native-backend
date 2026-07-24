package com.pk.core.profile;

public record ProfileDeviceData(
        long profileId,
        String partnerUserId,
        String deviceNo,
        String systemPlatform,
        String appName,
        String appVersion,
        String packageName,
        String phoneBrand,
        String phoneBrandModel,
        String mac,
        String systemVersion,
        String deliveryPlatform,
        Integer cpuCores,
        Long memoryTotal,
        Long sdCardTotal,
        String adId,
        String idfv,
        String idfa,
        String extParam,
        String ip,
        String deviceJson,
        String lastRequestId
) {
}
