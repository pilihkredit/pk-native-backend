package com.pk.infra.push.repository;

import com.pk.core.push.PushDeviceRegistration;
import com.pk.core.push.PushDeviceTarget;
import com.pk.core.push.port.PushDeviceRepository;
import com.pk.infra.push.mapper.PushDeviceMapper;
import java.util.List;
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

    @Override
    public List<PushDeviceTarget> findTargetsAfterId(long idExclusive, int limit) {
        return pushDeviceMapper.findTargetsAfterId(idExclusive, limit);
    }

    @Override
    public List<PushDeviceTarget> findTargetsByUserId(long userId) {
        return pushDeviceMapper.findTargetsByUserId(userId);
    }
}
