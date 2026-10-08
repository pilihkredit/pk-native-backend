package com.pk.infra.push.mapper;

import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushDisplayLogMapper {

    int insertDisplayedIfAbsent(
            @Param("userId") long userId,
            @Param("taskId") long taskId,
            @Param("displayedAt") Instant displayedAt
    );
}
