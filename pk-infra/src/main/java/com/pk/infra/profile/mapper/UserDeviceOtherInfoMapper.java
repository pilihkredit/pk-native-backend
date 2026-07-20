package com.pk.infra.profile.mapper;

import com.pk.core.profile.UserDeviceOtherInfoData;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserDeviceOtherInfoMapper {
    int upsert(UserDeviceOtherInfoData data);

    int deleteByDeviceNo(@Param("deviceNo") String deviceNo);
}
