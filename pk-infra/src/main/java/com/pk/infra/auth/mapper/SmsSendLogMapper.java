package com.pk.infra.auth.mapper;

import java.time.Instant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface SmsSendLogMapper {
    long countSince(
            @Param("mobileNo") String mobileNo,
            @Param("sinceInclusive") Instant sinceInclusive
    );

    int insertParam(com.pk.infra.auth.repository.SmsSendLogRepositoryImpl.SmsSendLogInsertParam param);

    int updateProviderResult(
            @Param("logId") long logId,
            @Param("providerCode") String providerCode,
            @Param("providerMessageId") String providerMessageId,
            @Param("providerSuccess") boolean providerSuccess,
            @Param("errorCode") String errorCode,
            @Param("errorMessage") String errorMessage
    );
}
