package com.pk.infra.debug.mapper;

import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface DebugTrackingReadMapper {
    long countByClientNo(@Param("clientNo") String clientNo);

    List<EventTypeCountRecord> countEventTypesByClientNo(@Param("clientNo") String clientNo);

    List<TrackingEventRecord> findByClientNo(
            @Param("clientNo") String clientNo,
            @Param("limit") int limit
    );

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
            Long profileId,
            String source,
            Instant createdAt
    ) {
    }
}
