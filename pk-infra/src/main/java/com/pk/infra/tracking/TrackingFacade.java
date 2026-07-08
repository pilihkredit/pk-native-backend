package com.pk.infra.tracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import com.pk.core.tracking.port.TrackingEventRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TrackingFacade {
    public static final int MAX_BATCH_SIZE = 50;
    private static final String SOURCE_CLIENT = "CLIENT";
    private static final ZoneId LENDER_DATETIME_ZONE = ZoneId.of("Asia/Jakarta");
    private static final DateTimeFormatter LENDER_DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(LENDER_DATETIME_ZONE);

    private final TrackingEventRepository trackingEventRepository;
    private final LenderTrackingPort lenderTrackingPort;
    private final ObjectMapper objectMapper;

    public TrackingFacade(
            TrackingEventRepository trackingEventRepository,
            LenderTrackingPort lenderTrackingPort,
            ObjectMapper objectMapper
    ) {
        this.trackingEventRepository = trackingEventRepository;
        this.lenderTrackingPort = lenderTrackingPort;
        this.objectMapper = objectMapper;
    }

    public IngestResult ingest(
            Long profileId,
            String partnerUserId,
            String deviceNo,
            String clientIp,
            List<TrackingEventCommand> events
    ) {
        if (events == null || events.isEmpty() || events.size() > MAX_BATCH_SIZE) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }

        List<TrackingEventCommand> validEvents = new ArrayList<>();
        int rejectedCount = 0;
        Set<String> seenInBatch = new HashSet<>();
        for (TrackingEventCommand event : events) {
            if (!isValid(event) || !seenInBatch.add(event.eventId())) {
                rejectedCount++;
                continue;
            }
            validEvents.add(event);
        }

        if (validEvents.isEmpty()) {
            return new IngestResult(0, rejectedCount);
        }

        Set<String> candidateIds = new LinkedHashSet<>();
        for (TrackingEventCommand event : validEvents) {
            candidateIds.add(event.eventId());
        }
        Set<String> existingIds = trackingEventRepository.findExistingEventIds(candidateIds);

        List<TrackingEventRepository.TrackingEventInsert> inserts = new ArrayList<>();
        for (TrackingEventCommand event : validEvents) {
            if (existingIds.contains(event.eventId())) {
                rejectedCount++;
                continue;
            }
            inserts.add(toInsert(event, profileId, partnerUserId, deviceNo));
        }

        if (!inserts.isEmpty()) {
            lenderTrackingPort.submitEvents(toLenderEvents(inserts, validEvents, partnerUserId, deviceNo, clientIp));
            trackingEventRepository.insertBatch(inserts);
        }

        return new IngestResult(inserts.size(), rejectedCount);
    }

    private List<LenderTrackingEvent> toLenderEvents(
            List<TrackingEventRepository.TrackingEventInsert> inserts,
            List<TrackingEventCommand> validEvents,
            String partnerUserId,
            String deviceNo,
            String clientIp
    ) {
        Set<String> acceptedIds = new HashSet<>();
        for (TrackingEventRepository.TrackingEventInsert insert : inserts) {
            acceptedIds.add(insert.eventId());
        }
        List<LenderTrackingEvent> lenderEvents = new ArrayList<>();
        for (TrackingEventCommand event : validEvents) {
            if (acceptedIds.contains(event.eventId())) {
                lenderEvents.add(toLenderEvent(event, partnerUserId, deviceNo, clientIp));
            }
        }
        return lenderEvents;
    }

    private LenderTrackingEvent toLenderEvent(
            TrackingEventCommand event,
            String partnerUserId,
            String deviceNo,
            String clientIp
    ) {
        return new LenderTrackingEvent(
                event.eventTime(),
                partnerUserId == null ? "" : partnerUserId,
                event.eventType(),
                event.url(),
                event.extend(),
                event.traceId(),
                firstPresent(event.clientNo(), deviceNo),
                event.clientManufacture(),
                event.clientModel(),
                event.clientCategory(),
                event.clientOs(),
                event.clientOsVersion(),
                event.ai(),
                event.av(),
                event.wv(),
                event.bn(),
                event.bv(),
                event.androidId(),
                event.gaid(),
                event.idfv(),
                event.idfa(),
                clientIp,
                LENDER_DATETIME_FORMATTER.format(Instant.ofEpochMilli(event.eventTime()))
        );
    }

    private TrackingEventRepository.TrackingEventInsert toInsert(
            TrackingEventCommand event,
            Long profileId,
            String partnerUserId,
            String deviceNo
    ) {
        return new TrackingEventRepository.TrackingEventInsert(
                event.eventId(),
                event.traceId(),
                partnerUserId,
                profileId,
                event.eventType(),
                Instant.ofEpochMilli(event.eventTime()),
                event.url(),
                deviceNo,
                serializeExtend(event.extend()),
                SOURCE_CLIENT
        );
    }

    private String serializeExtend(Map<String, Object> extend) {
        if (extend == null || extend.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(extend);
        } catch (JsonProcessingException exception) {
            return null;
        }
    }

    private static boolean isValid(TrackingEventCommand event) {
        return event != null
                && isPresent(event.eventId(), 128)
                && isPresent(event.eventType(), 128)
                && isPresent(event.traceId(), 64)
                && event.eventTime() != null
                && event.eventTime() > 0
                && (event.url() == null || event.url().length() <= 512);
    }

    private static boolean isPresent(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength;
    }

    private static String firstPresent(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        return second;
    }

    public record TrackingEventCommand(
            String eventId,
            String eventType,
            Long eventTime,
            String traceId,
            String url,
            Map<String, Object> extend,
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
            String idfa
    ) {
    }

    public record IngestResult(int acceptedCount, int rejectedCount) {
    }
}
