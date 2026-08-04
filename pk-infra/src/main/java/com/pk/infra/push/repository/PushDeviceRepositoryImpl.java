package com.pk.infra.push.repository;

import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.port.PushDeviceRepository;
import com.pk.infra.push.mapper.PushDeviceMapper;
import org.springframework.stereotype.Repository;

@Repository
public class PushDeviceRepositoryImpl implements PushDeviceRepository {
    private final PushDeviceMapper pushDeviceMapper;

    public PushDeviceRepositoryImpl(PushDeviceMapper pushDeviceMapper) {
        this.pushDeviceMapper = pushDeviceMapper;
    }

    @Override
    public void register(PushDeviceRegistration registration) {
        pushDeviceMapper.register(new PushDeviceRegistrationRow(
                registration.userId(),
                registration.deviceNo().trim(),
                registration.appVersion().trim(),
                registration.platform().trim(),
                registration.appPackage().trim(),
                registration.fcmToken().trim(),
                registration.permissionStatus().name()
        ));
    }

    @Override
    public void bindUser(long userId, String deviceNo) {
        pushDeviceMapper.bindUser(userId, deviceNo);
    }

    @Override
    public void unbindUser(long userId, String deviceNo) {
        pushDeviceMapper.unbindUser(userId, deviceNo);
    }
}
