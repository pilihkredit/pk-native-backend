package com.pk.app.debug.application;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugTrackingEventsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DebugTrackingEventsApplicationService {
    static final int DEFAULT_LIMIT = 200;
    static final int MAX_LIMIT = 500;

    private final DebugTrackingReadMapper readMapper;
    private final DebugUserProgressProperties properties;

    public DebugTrackingEventsApplicationService(
            DebugTrackingReadMapper readMapper,
            DebugUserProgressProperties properties
    ) {
        this.readMapper = readMapper;
        this.properties = properties;
    }

    public DebugTrackingEventsResponse query(String debugToken, String clientNo, Integer limit) {
        validateToken(debugToken);
        if (clientNo == null || clientNo.isBlank()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String normalized = clientNo.trim();
        int resolvedLimit = resolveLimit(limit);
        long total = readMapper.countByClientNo(normalized);
        if (total == 0L) {
            return DebugTrackingEventsResponse.empty(normalized);
        }
        List<DebugTrackingEventsResponse.EventTypeSummary> eventTypes = readMapper
                .countEventTypesByClientNo(normalized)
                .stream()
                .map(row -> new DebugTrackingEventsResponse.EventTypeSummary(row.eventType(), row.count()))
                .toList();
        List<DebugTrackingEventsResponse.TrackingEventInfo> events = readMapper
                .findByClientNo(normalized, resolvedLimit)
                .stream()
                .map(DebugTrackingEventsApplicationService::toEventInfo)
                .toList();
        return new DebugTrackingEventsResponse(
                true,
                normalized,
                total,
                events.size(),
                total > events.size(),
                eventTypes,
                events
        );
    }

    private void validateToken(String debugToken) {
        if (!properties.enabled() || !properties.tokenConfigured()) {
            throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
        }
        if (debugToken == null || !properties.token().equals(debugToken.trim())) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }

    private static int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }
        return Math.min(limit, MAX_LIMIT);
    }

    private static DebugTrackingEventsResponse.TrackingEventInfo toEventInfo(
            DebugTrackingReadMapper.TrackingEventRecord record
    ) {
        return new DebugTrackingEventsResponse.TrackingEventInfo(
                record.id(),
                record.eventTimestamp(),
                record.eventDatetime(),
                record.eventType(),
                record.url(),
                record.uid(),
                record.traceId(),
                record.clientManufacture(),
                record.clientModel(),
                record.clientCategory(),
                record.clientOs(),
                record.clientOsVersion(),
                record.ai(),
                record.av(),
                record.wv(),
                record.bn(),
                record.bv(),
                record.androidId(),
                record.gaid(),
                record.idfv(),
                record.idfa(),
                record.ip(),
                record.extendJson(),
                record.payloadJson(),
                record.partnerUserId(),
                record.profileId(),
                record.source(),
                record.createdAt()
        );
    }
}
