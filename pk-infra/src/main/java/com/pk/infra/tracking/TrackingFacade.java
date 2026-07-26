package com.pk.infra.tracking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.tracking.LenderTrackingEvent;
import com.pk.core.tracking.port.LenderTrackingPort;
import com.pk.core.tracking.port.TrackingEventRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class TrackingFacade {
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

    public void ingest(Long userId, String partnerUserId, String clientIp, TrackingEventCommand event) {
        if (!isValid(event)) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        String normalizedPartnerUserId = partnerUserId == null || partnerUserId.isBlank()
                ? null
                : partnerUserId.trim();
        String uid = resolveUid(event.uid(), normalizedPartnerUserId);
        String datetime = LENDER_DATETIME_FORMATTER.format(Instant.ofEpochMilli(event.timestamp()));
        String ip = clientIp == null ? "" : clientIp;

        LenderTrackingEvent lenderEvent = new LenderTrackingEvent(
                event.timestamp(),
                uid,
                event.eventType(),
                event.url(),
                event.extend(),
                event.traceId(),
                event.clientNo(),
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
                ip,
                datetime
        );

        String payloadJson = toPayloadJson(lenderEvent);
        TrackingEventRepository.TrackingEventInsert insert = new TrackingEventRepository.TrackingEventInsert(
                event.timestamp(),
                uid,
                event.eventType(),
                event.url(),
                serializeExtend(event.extend()),
                event.traceId(),
                event.clientNo(),
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
                ip,
                datetime,
                payloadJson,
                normalizedPartnerUserId,
                userId,
                SOURCE_CLIENT
        );

        lenderTrackingPort.submitEvents(List.of(lenderEvent));
        trackingEventRepository.insert(insert);
    }

    private String toPayloadJson(LenderTrackingEvent event) {
        ObjectNode node = objectMapper.createObjectNode();
        put(node, "timestamp", event.timestamp());
        put(node, "uid", event.uid());
        put(node, "eventType", event.eventType());
        put(node, "url", event.url());
        if (event.extend() != null && !event.extend().isEmpty()) {
            node.set("extend", objectMapper.valueToTree(event.extend()));
        }
        put(node, "traceId", event.traceId());
        put(node, "clientNo", event.clientNo());
        put(node, "clientManufacture", event.clientManufacture());
        put(node, "clientModel", event.clientModel());
        put(node, "clientCategory", event.clientCategory());
        put(node, "clientOs", event.clientOs());
        put(node, "clientOsVersion", event.clientOsVersion());
        put(node, "ai", event.ai());
        put(node, "av", event.av());
        put(node, "wv", event.wv());
        put(node, "bn", event.bn());
        put(node, "bv", event.bv());
        put(node, "androidId", event.androidId());
        put(node, "gaid", event.gaid());
        put(node, "idfv", event.idfv());
        put(node, "idfa", event.idfa());
        put(node, "ip", event.ip());
        put(node, "datetime", event.datetime());
        try {
            return objectMapper.writeValueAsString(node);
        } catch (JsonProcessingException exception) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
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
                && event.timestamp() != null
                && event.timestamp() > 0
                && isPresent(event.eventType(), 128)
                && isPresent(event.url(), 512)
                && isPresent(event.traceId(), 64)
                && isPresent(event.clientNo(), 128)
                && isPresent(event.clientManufacture(), 64)
                && isPresent(event.clientModel(), 128)
                && isPresent(event.clientCategory(), 32)
                && isPresent(event.clientOs(), 32)
                && isPresent(event.clientOsVersion(), 64)
                && isPresent(event.ai(), 128)
                && isPresent(event.av(), 64)
                && (event.uid() == null || event.uid().length() <= 128)
                && (event.wv() == null || event.wv().length() <= 64)
                && (event.bn() == null || event.bn().length() <= 64)
                && (event.bv() == null || event.bv().length() <= 64)
                && (event.androidId() == null || event.androidId().length() <= 128)
                && (event.gaid() == null || event.gaid().length() <= 128)
                && (event.idfv() == null || event.idfv().length() <= 128)
                && (event.idfa() == null || event.idfa().length() <= 128);
    }

    private static boolean isPresent(String value, int maxLength) {
        return value != null && !value.isBlank() && value.length() <= maxLength;
    }

    private static String resolveUid(String requestUid, String partnerUserId) {
        if (requestUid != null && !requestUid.isBlank()) {
            return requestUid.trim();
        }
        // Logged-in: fill from partner user id; anonymous may remain blank.
        return partnerUserId == null ? "" : partnerUserId;
    }

    private static void put(ObjectNode node, String field, String value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    private static void put(ObjectNode node, String field, Long value) {
        if (value != null) {
            node.put(field, value);
        }
    }

    public record TrackingEventCommand(
            Long timestamp,
            String uid,
            String eventType,
            String url,
            Map<String, Object> extend,
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
            String idfa
    ) {
    }
}
