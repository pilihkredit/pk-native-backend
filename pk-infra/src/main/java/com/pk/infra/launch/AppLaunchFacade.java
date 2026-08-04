package com.pk.infra.launch;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.launch.AppLaunchRecord;
import com.pk.core.launch.port.AppLaunchRepository;
import java.time.Instant;

public class AppLaunchFacade {
    private final AppLaunchRepository appLaunchRepository;

    public AppLaunchFacade(AppLaunchRepository appLaunchRepository) {
        this.appLaunchRepository = appLaunchRepository;
    }

    public boolean record(AppLaunchRecord record) {
        if (record == null
                || isBlank(record.launchId())
                || record.launchId().length() > 64
                || isBlank(record.deviceNo())
                || isBlank(record.appVersion())
                || isBlank(record.platform())
                || isBlank(record.appPackage())
                || record.occurredAt() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return appLaunchRepository.record(record);
    }

    public long countEvents(Instant fromInclusive, Instant toExclusive, String deviceNo) {
        if (fromInclusive == null || toExclusive == null || !fromInclusive.isBefore(toExclusive)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return appLaunchRepository.countEvents(fromInclusive, toExclusive, normalizeDeviceNo(deviceNo));
    }

    private static String normalizeDeviceNo(String deviceNo) {
        return isBlank(deviceNo) ? null : deviceNo.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
