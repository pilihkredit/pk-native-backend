package com.pk.infra.attribution;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.pk.core.attribution.port.AdjustConfigRepository;
import com.pk.core.attribution.port.AdjustEventRecordRepository;
import com.pk.core.attribution.port.AppsFlyerS2sReporter;
import com.pk.core.callback.port.ServerEventCallbackParser;
import com.pk.core.profile.ProfileAfData;
import com.pk.core.profile.ProfileDeviceData;
import com.pk.core.profile.port.ProfileAfRepository;
import com.pk.core.profile.port.ProfileDeviceRepository;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppsFlyerS2sReporterImpl implements AppsFlyerS2sReporter {
    private static final Logger log = LoggerFactory.getLogger(AppsFlyerS2sReporterImpl.class);
    private static final DateTimeFormatter EVENT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final int STATUS_PENDING = 0;
    private static final int STATUS_SUCCESS = 1;
    private static final int STATUS_FAILED = 2;

    private final AdjustConfigRepository adjustConfigRepository;
    private final AdjustEventRecordRepository adjustEventRecordRepository;
    private final ProfileDeviceRepository profileDeviceRepository;
    private final ProfileAfRepository profileAfRepository;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AppsFlyerS2sReporterImpl(
            AdjustConfigRepository adjustConfigRepository,
            AdjustEventRecordRepository adjustEventRecordRepository,
            ProfileDeviceRepository profileDeviceRepository,
            ProfileAfRepository profileAfRepository,
            ObjectMapper objectMapper
    ) {
        this.adjustConfigRepository = adjustConfigRepository;
        this.adjustEventRecordRepository = adjustEventRecordRepository;
        this.profileDeviceRepository = profileDeviceRepository;
        this.profileAfRepository = profileAfRepository;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    }

    @Override
    public ReportResult report(long serverEventCallbackId, ServerEventCallbackParser.ParsedServerEventCallback event) {
        String osName = normalizeOsName(event.systemPlatform());
        Optional<AdjustConfigRepository.AdjustConfigData> configOpt = adjustConfigRepository.findActiveByOsName(osName);
        if (configOpt.isEmpty()) {
            return skipRecorded(serverEventCallbackId, event, null, null, "no active AF config for os=" + osName);
        }
        AdjustConfigRepository.AdjustConfigData config = configOpt.get();

        DeviceIds deviceIds = resolveDeviceIds(event);
        if (isBlank(deviceIds.appsflyerId())) {
            return skipRecorded(
                    serverEventCallbackId,
                    event,
                    config.appToken(),
                    deviceIds,
                    "appsflyer_id missing"
            );
        }

        String extraParams = buildExtraParams(event);
        long recordId = adjustEventRecordRepository.insert(new AdjustEventRecordRepository.AdjustEventRecordInsert(
                serverEventCallbackId,
                event.partnerUserId(),
                resolveUserId(event.deviceNo()),
                event.deviceNo(),
                event.eventType(),
                config.appToken(),
                deviceIds.idfa(),
                deviceIds.idfv(),
                deviceIds.gpsAdid(),
                deviceIds.appsflyerId(),
                event.eventTime() == null ? Instant.now().toEpochMilli() : event.eventTime(),
                STATUS_PENDING,
                0,
                extraParams
        ));

        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("eventName", event.eventType());
            payload.put("eventTime", formatEventTime(event.eventTime()));
            payload.put("appsflyer_id", deviceIds.appsflyerId());
            if (!isBlank(event.partnerUserId())) {
                payload.put("customer_user_id", event.partnerUserId());
            }
            if (!isBlank(deviceIds.idfa())) {
                payload.put("idfa", deviceIds.idfa());
            }
            if (!isBlank(deviceIds.idfv())) {
                payload.put("idfv", deviceIds.idfv());
            }
            if (!isBlank(deviceIds.gpsAdid())) {
                payload.put("advertising_id", deviceIds.gpsAdid());
            }
            payload.put("eventValue", event.eventValue() == null ? "" : event.eventValue());

            String requestBody = objectMapper.writeValueAsString(payload);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(config.baseUrl()))
                    .timeout(Duration.ofMillis(Math.max(1_000, config.timeout())))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("authentication", config.apiToken())
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            boolean success = response.statusCode() >= 200 && response.statusCode() < 300;
            String responseBody = response.body() == null ? "" : response.body();
            adjustEventRecordRepository.updateStatus(
                    recordId,
                    success ? STATUS_SUCCESS : STATUS_FAILED,
                    responseBody,
                    success ? null : "HTTP_" + response.statusCode(),
                    0
            );
            if (!success) {
                log.warn(
                        "AppsFlyer S2S failed: serverEventCallbackId={}, eventType={}, status={}",
                        serverEventCallbackId,
                        event.eventType(),
                        response.statusCode()
                );
            }
            return ReportResult.recorded(recordId, success ? "OK" : "HTTP_" + response.statusCode());
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            adjustEventRecordRepository.updateStatus(recordId, STATUS_FAILED, null, exception.getMessage(), 0);
            return ReportResult.recorded(recordId, "interrupted");
        } catch (Exception exception) {
            log.error(
                    "AppsFlyer S2S error: serverEventCallbackId={}, eventType={}, error={}",
                    serverEventCallbackId,
                    event.eventType(),
                    exception.getMessage(),
                    exception
            );
            adjustEventRecordRepository.updateStatus(recordId, STATUS_FAILED, null, exception.getMessage(), 0);
            return ReportResult.recorded(recordId, exception.getMessage());
        }
    }

    /**
     * Persist skip reasons into adjust_event_record so ops can see why AF was not sent.
     * status=2 (failed), error_message=skip reason.
     */
    private ReportResult skipRecorded(
            long serverEventCallbackId,
            ServerEventCallbackParser.ParsedServerEventCallback event,
            String appToken,
            DeviceIds deviceIds,
            String reason
    ) {
        try {
            long recordId = adjustEventRecordRepository.insert(new AdjustEventRecordRepository.AdjustEventRecordInsert(
                    serverEventCallbackId,
                    event.partnerUserId(),
                    resolveUserId(event.deviceNo()),
                    event.deviceNo(),
                    event.eventType(),
                    appToken,
                    deviceIds == null ? null : deviceIds.idfa(),
                    deviceIds == null ? null : deviceIds.idfv(),
                    deviceIds == null ? null : deviceIds.gpsAdid(),
                    deviceIds == null ? null : deviceIds.appsflyerId(),
                    event.eventTime() == null ? Instant.now().toEpochMilli() : event.eventTime(),
                    STATUS_FAILED,
                    0,
                    buildExtraParams(event)
            ));
            adjustEventRecordRepository.updateStatus(recordId, STATUS_FAILED, null, reason, 0);
            log.warn(
                    "AppsFlyer S2S skipped: serverEventCallbackId={}, eventType={}, reason={}",
                    serverEventCallbackId,
                    event.eventType(),
                    reason
            );
            return ReportResult.skipped(recordId, reason);
        } catch (Exception exception) {
            log.error(
                    "AppsFlyer S2S skip persist failed: serverEventCallbackId={}, eventType={}, reason={}, error={}",
                    serverEventCallbackId,
                    event.eventType(),
                    reason,
                    exception.getMessage(),
                    exception
            );
            return ReportResult.skipped(reason);
        }
    }

    private DeviceIds resolveDeviceIds(ServerEventCallbackParser.ParsedServerEventCallback event) {
        String appsflyerId = null;
        String idfa = null;
        String idfv = null;
        String gpsAdid = event.adId();
        if (!isBlank(event.deviceNo())) {
            Optional<ProfileDeviceData> device = profileDeviceRepository.findByDeviceNo(event.deviceNo());
            if (device.isPresent()) {
                ProfileDeviceData data = device.get();
                if (isBlank(gpsAdid)) {
                    gpsAdid = data.adId();
                }
                DeviceIds fromJson = extractFromDeviceJson(data.deviceJson());
                appsflyerId = fromJson.appsflyerId();
                idfa = firstNonBlank(data.idfa(), fromJson.idfa());
                idfv = firstNonBlank(data.idfv(), fromJson.idfv());
                if (isBlank(gpsAdid)) {
                    gpsAdid = fromJson.gpsAdid();
                }
            }
            // Client install API stores AF id in user_profile_af, not always in device_json.
            if (isBlank(appsflyerId) || isBlank(idfa) || isBlank(idfv) || isBlank(gpsAdid)) {
                Optional<ProfileAfData> af = profileAfRepository.findLatestByDeviceNo(event.deviceNo());
                if (af.isPresent()) {
                    ProfileAfData data = af.get();
                    appsflyerId = firstNonBlank(appsflyerId, data.appsflyerId());
                    idfa = firstNonBlank(idfa, data.idfa());
                    idfv = firstNonBlank(idfv, data.idfv());
                    gpsAdid = firstNonBlank(gpsAdid, data.advertisingId(), data.afAdId());
                }
            }
        }
        return new DeviceIds(appsflyerId, idfa, idfv, gpsAdid);
    }

    private DeviceIds extractFromDeviceJson(String deviceJson) {
        if (isBlank(deviceJson)) {
            return new DeviceIds(null, null, null, null);
        }
        try {
            JsonNode root = objectMapper.readTree(deviceJson);
            return new DeviceIds(
                    firstText(root, "appsflyerId", "appsflyer_id", "adid"),
                    firstText(root, "idfa"),
                    firstText(root, "idfv"),
                    firstText(root, "gaid", "gpsAdid", "advertisingId", "adId")
            );
        } catch (Exception exception) {
            return new DeviceIds(null, null, null, null);
        }
    }

    private Long resolveUserId(String deviceNo) {
        if (isBlank(deviceNo)) {
            return null;
        }
        return profileDeviceRepository.findByDeviceNo(deviceNo).map(ProfileDeviceData::userId).orElse(null);
    }

    private String buildExtraParams(ServerEventCallbackParser.ParsedServerEventCallback event) {
        try {
            ObjectNode node = objectMapper.createObjectNode();
            put(node, "eventId", event.eventId());
            put(node, "eventValue", event.eventValue());
            if (event.value() != null) {
                node.put("value", event.value());
            }
            put(node, "clientId", event.clientId());
            put(node, "userId", event.userId());
            put(node, "partnerUserId", event.partnerUserId());
            put(node, "appName", event.appName());
            put(node, "countryCode", event.countryCode());
            put(node, "appVersion", event.appVersion());
            put(node, "countryName", event.countryName());
            put(node, "deviceNo", event.deviceNo());
            put(node, "systemPlatform", event.systemPlatform());
            put(node, "adId", event.adId());
            return objectMapper.writeValueAsString(node);
        } catch (Exception exception) {
            return null;
        }
    }

    private static void put(ObjectNode node, String field, String value) {
        if (!isBlank(value)) {
            node.put(field, value);
        }
    }

    private static String formatEventTime(Long eventTime) {
        Instant instant = eventTime == null ? Instant.now() : Instant.ofEpochMilli(eventTime);
        return LocalDateTime.ofInstant(instant, ZoneOffset.UTC).format(EVENT_TIME_FORMATTER);
    }

    private static String normalizeOsName(String systemPlatform) {
        if (isBlank(systemPlatform)) {
            return "Android";
        }
        String value = systemPlatform.trim();
        if ("ios".equalsIgnoreCase(value) || "iphone".equalsIgnoreCase(value)) {
            return "iOS";
        }
        return "Android";
    }

    private static String firstText(JsonNode root, String... fields) {
        for (String field : fields) {
            JsonNode node = root.get(field);
            if (node != null && !node.isNull()) {
                String value = node.asText();
                if (!isBlank(value)) {
                    return value.trim();
                }
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record DeviceIds(String appsflyerId, String idfa, String idfv, String gpsAdid) {
    }
}
