package com.pk.infra.debug.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugUserProgressReadMapper {
    List<String> findCreditApplyIds(@Param("userId") long userId);

    List<String> findLoanApplyIds(@Param("userId") long userId);

    List<InteractionRecord> findInteractionsByMobileNo(
            @Param("mobileNo") String mobileNo,
            @Param("businessIds") List<String> businessIds
    );

    record InteractionRecord(
            long id,
            String providerCode,
            String interactionNo,
            String businessType,
            String businessId,
            String httpMethod,
            String endpoint,
            String responseCode,
            String responseMsg,
            boolean success,
            Integer durationMs,
            String requestRef,
            String responseRef,
            Instant createdAt
    ) {
    }
}
