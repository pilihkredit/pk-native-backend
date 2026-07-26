package com.pk.app.debug.application;

import com.pk.app.debug.config.DebugUserProgressProperties;
import com.pk.app.debug.dto.DebugTrackingEventsResponse;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.auth.UserProfileSummary;
import com.pk.core.auth.port.UserAuthRepository;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper;
import com.pk.infra.debug.mapper.DebugTrackingReadMapper.TrackingQueryCriteria;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class DebugTrackingEventsApplicationService {
    static final int DEFAULT_LIMIT = 200;
    static final int MAX_LIMIT = 500;

    private final DebugTrackingReadMapper readMapper;
    private final UserAuthRepository userAuthRepository;
    private final DebugUserProgressProperties properties;

    public DebugTrackingEventsApplicationService(
            DebugTrackingReadMapper readMapper,
            UserAuthRepository userAuthRepository,
            DebugUserProgressProperties properties
    ) {
        this.readMapper = readMapper;
        this.userAuthRepository = userAuthRepository;
        this.properties = properties;
    }

    public DebugTrackingEventsResponse query(
            String debugToken,
            String clientNo,
            String mobileNo,
            String requestedUserId,
            Integer limit
    ) {
        validateToken(debugToken);
        String normalizedClientNo = blankToNull(clientNo);
        String normalizedMobileNo = blankToNull(mobileNo);
        String normalizedRequestedUserId = blankToNull(requestedUserId);
        if (normalizedClientNo == null && normalizedMobileNo == null && normalizedRequestedUserId == null) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        Long userId = null;
        String partnerUserId = null;
        Set<String> userIds = new LinkedHashSet<>();

        if (normalizedMobileNo != null) {
            Optional<UserProfileSummary> user = userAuthRepository.findByMobileNo(normalizedMobileNo);
            if (user.isEmpty()) {
                return DebugTrackingEventsResponse.empty(
                        normalizedClientNo,
                        normalizedMobileNo,
                        normalizedRequestedUserId,
                        null,
                        null
                );
            }
            userId = user.get().userId();
            partnerUserId = user.get().partnerUserId();
            if (partnerUserId != null && !partnerUserId.isBlank()) {
                userIds.add(partnerUserId.trim());
            }
        }

        if (normalizedRequestedUserId != null) {
            userIds.add(normalizedRequestedUserId);
            Long parsedUserId = parseUserId(normalizedRequestedUserId);
            if (parsedUserId != null) {
                userId = userId == null ? parsedUserId : userId;
                Optional<UserProfileSummary> byProfile = userAuthRepository.findByUserId(parsedUserId);
                if (byProfile.isPresent()) {
                    partnerUserId = partnerUserId == null ? byProfile.get().partnerUserId() : partnerUserId;
                    if (byProfile.get().partnerUserId() != null && !byProfile.get().partnerUserId().isBlank()) {
                        userIds.add(byProfile.get().partnerUserId().trim());
                    }
                }
            }
        }

        TrackingQueryCriteria criteria = new TrackingQueryCriteria(
                normalizedClientNo,
                userId,
                List.copyOf(userIds)
        );
        if (normalizedClientNo == null && !criteria.hasUserScope()) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        int resolvedLimit = resolveLimit(limit);
        long total = readMapper.countByCriteria(criteria);
        if (total == 0L) {
            return DebugTrackingEventsResponse.empty(
                    normalizedClientNo,
                    normalizedMobileNo,
                    normalizedRequestedUserId,
                    userId,
                    partnerUserId
            );
        }
        List<DebugTrackingEventsResponse.EventTypeSummary> eventTypes = readMapper
                .countEventTypesByCriteria(criteria)
                .stream()
                .map(row -> new DebugTrackingEventsResponse.EventTypeSummary(row.eventType(), row.count()))
                .toList();
        List<DebugTrackingEventsResponse.TrackingEventInfo> events = readMapper
                .findByCriteria(criteria, resolvedLimit)
                .stream()
                .map(DebugTrackingEventsApplicationService::toEventInfo)
                .toList();
        return new DebugTrackingEventsResponse(
                true,
                normalizedClientNo,
                normalizedMobileNo,
                normalizedRequestedUserId,
                userId,
                partnerUserId,
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

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static Long parseUserId(String userId) {
        if (!userId.chars().allMatch(Character::isDigit)) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException exception) {
            return null;
        }
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
                record.clientNo(),
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
                record.userId(),
                record.source(),
                record.createdAt()
        );
    }
}
