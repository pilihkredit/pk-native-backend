package com.pk.infra.push.mapper;

import com.pk.core.push.PushDeviceTarget;
import com.pk.infra.push.repository.PushDeviceRegistrationRow;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushDeviceMapper {
    int register(PushDeviceRegistrationRow row);

    int bindUser(@Param("userId") long userId, @Param("deviceNo") String deviceNo);

    int unbindUser(@Param("userId") long userId, @Param("deviceNo") String deviceNo);

    List<PushDeviceTarget> findTargetsAfterId(@Param("idExclusive") long idExclusive, @Param("limit") int limit);

    List<PushDeviceTarget> findTargetsByUserId(@Param("userId") long userId);
}
