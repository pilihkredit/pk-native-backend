package com.pk.infra.push.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PushAudienceMapper {

    List<Long> findUserIdsByTaskId(@Param("taskId") long taskId);
}
