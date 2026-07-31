package com.pk.infra.profile.repository;

import com.pk.core.profile.UserDeviceOtherInfoData;
import com.pk.core.profile.port.UserDeviceOtherInfoRepository;
import com.pk.infra.profile.mapper.UserDeviceOtherInfoMapper;
import org.springframework.stereotype.Repository;

@Repository
public class UserDeviceOtherInfoRepositoryImpl implements UserDeviceOtherInfoRepository {
    private final UserDeviceOtherInfoMapper mapper;

    public UserDeviceOtherInfoRepositoryImpl(UserDeviceOtherInfoMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void upsert(UserDeviceOtherInfoData data) {
        mapper.upsert(data);
    }

    @Override
    public void deleteByDeviceNo(String deviceNo) {
        mapper.deleteByDeviceNo(deviceNo.trim());
    }
}
