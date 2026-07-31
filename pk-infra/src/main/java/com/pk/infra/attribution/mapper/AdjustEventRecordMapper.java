package com.pk.infra.attribution.mapper;

import com.pk.infra.attribution.repository.AdjustEventRecordInsertParam;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdjustEventRecordMapper {
    int insert(AdjustEventRecordInsertParam param);

    int updateStatus(
            @Param("id") long id,
            @Param("status") int status,
            @Param("response") String response,
            @Param("errorMessage") String errorMessage,
            @Param("retryCount") int retryCount
    );
}
