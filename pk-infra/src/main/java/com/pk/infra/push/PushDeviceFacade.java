package com.pk.infra.push;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.port.PushDeviceRepository;

public class PushDeviceFacade {
    private final PushDeviceRepository pushDeviceRepository;

    public PushDeviceFacade(PushDeviceRepository pushDeviceRepository) {
        this.pushDeviceRepository = pushDeviceRepository;
    }

    public void register(PushDeviceRegistration registration) {
        if (registration == null
                || isBlank(registration.deviceNo())
                || isBlank(registration.appVersion())
                || isBlank(registration.platform())
                || isBlank(registration.appPackage())
                || isBlank(registration.fcmToken())
                || registration.permissionStatus() == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        pushDeviceRepository.register(registration);
    }

    public void bindUser(long userId, String deviceNo) {
        if (userId > 0L && !isBlank(deviceNo)) {
            pushDeviceRepository.bindUser(userId, deviceNo.trim());
        }
    }

    public void unbindUser(long userId, String deviceNo) {
        if (userId > 0L && !isBlank(deviceNo)) {
            pushDeviceRepository.unbindUser(userId, deviceNo.trim());
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
