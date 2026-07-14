package com.pk.infra.appconfig.mapper;

import com.pk.core.appconfig.port.AppConfigRepository.AppConfigRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppConfigMapper {
    AppConfigRecord findByKey(@Param("key") String key);
}
