package com.pk.infra.debug;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugUserProgressReadMapper {
    List<String> findCreditApplyIds(@Param("profileId") long profileId);

    List<String> findLoanApplyIds(@Param("profileId") long profileId);

    List<InteractionRecord> findRecentInteractions(
            @Param("businessIds") List<String> businessIds,
            @Param("partnerUserId") String partnerUserId,
            @Param("mobileNo") String mobileNo,
            @Param("limit") int limit
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
