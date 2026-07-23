package com.pk.infra.external.mapper;

import com.pk.core.external.ExternalInteractionCallbackLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ExternalInteractionCallbackLogMapper {
    int insert(ExternalInteractionCallbackLog log);

    Long findIdByIdempotencyKey(@Param("idempotencyKey") String idempotencyKey);

    int updateResponse(
            @Param("id") long id,
            @Param("mobileNo") String mobileNo,
            @Param("responseCode") String responseCode,
            @Param("responseMsg") String responseMsg,
            @Param("responseRef") String responseRef,
            @Param("success") boolean success,
            @Param("durationMs") int durationMs
    );
}
