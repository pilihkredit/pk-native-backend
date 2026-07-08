package com.pk.infra.attribution.mapper;

import com.pk.core.attribution.port.AdjustEventConfigRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdjustEventConfigMapper {
    AdjustEventConfigRepository.AdjustEventConfigData findEnabledByEventNameAndAppToken(
            @Param("eventName") String eventName,
            @Param("appToken") String appToken
    );
}
