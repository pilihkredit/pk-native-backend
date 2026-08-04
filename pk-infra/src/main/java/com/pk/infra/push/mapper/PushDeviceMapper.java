package com.pk.infra.push.mapper;

import com.pk.infra.push.repository.PushDeviceRegistrationRow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushDeviceMapper {
    int register(PushDeviceRegistrationRow row);

    int bindUser(@Param("userId") long userId, @Param("deviceNo") String deviceNo);

    int unbindUser(@Param("userId") long userId, @Param("deviceNo") String deviceNo);
}
