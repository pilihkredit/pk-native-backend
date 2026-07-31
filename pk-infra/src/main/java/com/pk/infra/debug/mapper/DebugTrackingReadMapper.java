package com.pk.infra.debug.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugTrackingReadMapper {
    long countByCriteria(@Param("criteria") TrackingQueryCriteria criteria);

    List<EventTypeCountRecord> countEventTypesByCriteria(@Param("criteria") TrackingQueryCriteria criteria);

    List<TrackingEventRecord> findByCriteria(
            @Param("criteria") TrackingQueryCriteria criteria,
            @Param("limit") int limit
    );

    record TrackingQueryCriteria(
            String clientNo,
            Long userId,
            List<String> userIds
    ) {
        public boolean hasUserScope() {
            return userId != null || (userIds != null && !userIds.isEmpty());
        }
    }

    record EventTypeCountRecord(
            String eventType,
            long count
    ) {
    }

    record TrackingEventRecord(
            long id,
            Long eventTimestamp,
            String uid,
            String eventType,
            String url,
            String extendJson,
            String traceId,
            String clientNo,
            String clientManufacture,
            String clientModel,
            String clientCategory,
            String clientOs,
            String clientOsVersion,
            String ai,
            String av,
            String wv,
            String bn,
            String bv,
            String androidId,
            String gaid,
            String idfv,
            String idfa,
            String ip,
            String eventDatetime,
            String payloadJson,
            String partnerUserId,
            Long userId,
            String source,
            Instant createdAt
    ) {
    }
}
