package com.pk.infra.attribution.mapper;

import com.pk.core.attribution.port.AdjustConfigRepository;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdjustConfigMapper {
    AdjustConfigRepository.AdjustConfigData findActiveByOsName(@Param("osName") String osName);
}
