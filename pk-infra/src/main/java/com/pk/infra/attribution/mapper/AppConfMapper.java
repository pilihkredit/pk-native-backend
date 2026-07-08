package com.pk.infra.attribution.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppConfMapper {
    String findConfigValue(@Param("configKey") String configKey);
}
