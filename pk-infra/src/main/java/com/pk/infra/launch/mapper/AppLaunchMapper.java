package com.pk.infra.launch.mapper;

import com.pk.core.launch.AppLaunchRecord;
import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AppLaunchMapper {
    int insertEvent(AppLaunchRecord record);

    long countEvents(
            @Param("fromInclusive") Instant fromInclusive,
            @Param("toExclusive") Instant toExclusive,
            @Param("deviceNo") String deviceNo
    );
}
