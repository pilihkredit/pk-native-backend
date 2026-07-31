package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.logging.LogContext;
import com.pk.core.profile.ocr.OcrCallContext;
import com.pk.core.profile.ocr.OcrCallContextHolder;
import com.pk.core.profile.ocr.OcrVendorCallStatus;
import com.pk.core.profile.ocr.OcrVendorOperationType;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import com.pk.core.profile.port.TrustDecisionKycPort;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.List;

public class TrustDecisionKycClient implements TrustDecisionKycPort {
    private static final String CHANNEL = "trustDecision";

    private final TrustDecisionProperties properties;
    private final ObjectMapper objectMapper;
    private final OcrVendorCallLogWriter callLogWriter;
    private final OcrSensitiveJsonSupport sensitiveJsonSupport;
    private final HttpClient httpClient;

    public TrustDecisionKycClient(
            TrustDecisionProperties properties,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter callLogWriter,
            OcrSensitiveJsonSupport sensitiveJsonSupport
    ) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.callLogWriter = callLogWriter;
        this.sensitiveJsonSupport = sensitiveJsonSupport;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    @Override
    public OcrResult checkIdentityCard(byte[] imageBytes) {
        JsonNode response = call(
                OcrVendorOperationType.OCR_CHECK,
                properties.ocrUrl(),
                Map.of("image", encode(imageBytes), "country", "ID", "options", "document_forgery,images"),
                "{\"image\":\"[protected]\",\"country\":\"ID\",\"options\":\"document_forgery,images\"}"
        );
        return new OcrResult(
                text(response, "result"),
                text(response, "sequence_id"),
                response.toString(),
                TrustDecisionOcrParser.parse(response)
        );
    }

    @Override
    public LivenessLicense obtainLivenessLicense(int sessionDurationSeconds) {
        JsonNode response = call(
                OcrVendorOperationType.LIVENESS_LICENSE,
                properties.livenessLicenseUrl(),
                Map.of("session_duration", sessionDurationSeconds),
                "{\"session_duration\":" + sessionDurationSeconds + "}"
        );
        return new LivenessLicense(
                text(response, "license"),
                response.path("expiry_timestamp").asLong(),
                text(response, "sequence_id")
        );
    }

    @Override
    public SdkLivenessResult retrieveLivenessResult(String livenessId) {
        JsonNode response = call(
                OcrVendorOperationType.LIVENESS_RESULT,
                properties.livenessResultUrl(),
                Map.of("liveness_id", livenessId),
                "{\"liveness_id\":\"[protected]\"}",
                Set.of(200, 12201, 12221, 12230, 12231, 12250)
        );
        int code = response.path("code").asInt();
        byte[] image = decode(text(response, "image"));
        List<String> riskTags = new java.util.ArrayList<>();
        response.path("device_risk_tag").forEach(node -> riskTags.add(node.asText()));
        return new SdkLivenessResult(
                code == 200 ? "pass" : "fail",
                text(response, "sequence_id"),
                image,
                List.copyOf(riskTags),
                response.path("device_risk_level").asInt()
        );
    }

    private JsonNode call(
            OcrVendorOperationType operationType,
            String endpoint,
            Map<String, Object> requestData,
            String auditRequestJson
    ) {
        return call(operationType, endpoint, requestData, auditRequestJson, Set.of(200));
    }

    private JsonNode call(
            OcrVendorOperationType operationType,
            String endpoint,
            Map<String, Object> requestData,
            String auditRequestJson,
            Set<Integer> acceptedCodes
    ) {
        URI uri = TrustDecisionHttpSupport.authenticatedUri(
                endpoint,
                properties.partnerCode(),
                properties.partnerKey()
        );
        String responseText = "";
        Integer httpStatus = null;
        String vendorCode = null;
        String vendorMessage = null;
        BigDecimal score = null;
        OcrVendorCallStatus status = OcrVendorCallStatus.EXCEPTION;
        String apiCode = null;
        long startedAt = System.currentTimeMillis();
        try {
            String requestJson = objectMapper.writeValueAsString(requestData);
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            httpStatus = response.statusCode();
            responseText = response.body() == null ? "" : response.body();
            if (httpStatus >= 400) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            JsonNode root = objectMapper.readTree(responseText);
            vendorCode = Integer.toString(root.path("code").asInt());
            vendorMessage = text(root, "message");
            score = extractScore(root);
            if (!acceptedCodes.contains(root.path("code").asInt())) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            status = OcrVendorCallStatus.SUCCESS;
            return root;
        } catch (ApiException exception) {
            apiCode = exception.apiCode().code();
            throw exception;
        } catch (java.net.http.HttpTimeoutException exception) {
            status = OcrVendorCallStatus.TIMEOUT;
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } catch (Exception exception) {
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } finally {
            String sanitizedResponse = sensitiveJsonSupport.sanitizeForStorage(responseText, currentMobileNo());
            writeAudit(
                    operationType,
                    status,
                    apiCode,
                    vendorCode,
                    vendorMessage,
                    score,
                    TrustDecisionHttpSupport.withoutQuery(uri),
                    httpStatus,
                    (int) (System.currentTimeMillis() - startedAt),
                    auditRequestJson,
                    sanitizedResponse
            );
        }
    }

    private void writeAudit(
            OcrVendorOperationType operationType,
            OcrVendorCallStatus status,
            String apiCode,
            String vendorCode,
            String vendorMessage,
            BigDecimal score,
            String endpoint,
            Integer httpStatus,
            Integer durationMs,
            String requestJson,
            String responseJson
    ) {
        OcrCallContext context = OcrCallContextHolder.get();
        String traceId = context == null ? LogContext.traceId() : context.traceId();
        callLogWriter.write(new OcrVendorCallLogWriter.OcrVendorCallLogEntry(
                context == null ? null : context.userId(),
                context == null ? null : context.partnerUserId(),
                context == null ? null : context.mobileNo(),
                operationType,
                CHANNEL,
                traceId,
                context == null ? traceId : context.clientRequestId(),
                status,
                apiCode,
                vendorCode,
                vendorMessage,
                score,
                null,
                endpoint,
                httpStatus,
                durationMs,
                requestJson,
                responseJson,
                null,
                null
        ));
    }

    private static BigDecimal extractScore(JsonNode root) {
        if (root.has("score")) {
            return BigDecimal.valueOf(root.path("score").asDouble());
        }
        if (root.has("similarity")) {
            return BigDecimal.valueOf(root.path("similarity").asDouble());
        }
        return null;
    }

    private static String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return value == null || value.isNull() ? null : value.asText();
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String value) {
        if (value == null || value.isBlank()) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
    }

    private static String currentMobileNo() {
        OcrCallContext context = OcrCallContextHolder.get();
        return context == null ? null : context.mobileNo();
    }
}
