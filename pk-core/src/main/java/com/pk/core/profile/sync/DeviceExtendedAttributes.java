package com.pk.core.profile.sync;

public record DeviceExtendedAttributes(
        String phoneBrand,
        String phoneBrandModel,
        String mac,
        String systemVersion,
        String deliveryPlatform,
        Integer cpuCores,
        Long memoryTotal,
        Long sdCardTotal,
        String idfv,
        String idfa,
        String extParam,
        String adChannel,
        String ip
) {
    public static DeviceExtendedAttributes empty() {
        return new DeviceExtendedAttributes(
                null, null, null, null, null, null, null, null, null, null, null, null, null
        );
    }
}
