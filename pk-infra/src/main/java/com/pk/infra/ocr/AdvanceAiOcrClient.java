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
import com.pk.core.profile.port.AdvanceAiOcrPort;
import com.pk.core.profile.port.OcrVendorCallLogWriter;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AdvanceAiOcrClient implements AdvanceAiOcrPort {
    private static final Logger log = LoggerFactory.getLogger(AdvanceAiOcrClient.class);

    private final OcrProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final OcrVendorCallLogWriter callLogWriter;
    private final OcrSensitiveJsonSupport sensitiveJsonSupport;
    private final HttpClient httpClient;

    public AdvanceAiOcrClient(
            OcrProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper,
            OcrVendorCallLogWriter callLogWriter,
            OcrSensitiveJsonSupport sensitiveJsonSupport
    ) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.callLogWriter = callLogWriter;
        this.sensitiveJsonSupport = sensitiveJsonSupport;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    @Override
    public LicenseTokenResult getLicenseToken(Long licenseEffectiveSeconds) {
        long effectiveSeconds = licenseEffectiveSeconds == null || licenseEffectiveSeconds <= 0
                ? properties.licenseEffectiveSeconds()
                : licenseEffectiveSeconds;
        String token = refreshAccessToken();
        Map<String, Object> requestData = Map.of("licenseEffectiveSeconds", effectiveSeconds);
        JsonNode response = postJson(
                OcrVendorOperationType.LICENSE_TOKEN,
                "license-token",
                properties.licenseUrl(),
                requestData,
                token
        );
        String code = text(response, "code");
        if (!"SUCCESS".equals(code)) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR, "OCR license token failed");
        }
        JsonNode data = response.get("data");
        String license = text(data, "license");
        if (license == null || license.isBlank()) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR, "OCR license token failed");
        }
        return new LicenseTokenResult(license, effectiveSeconds);
    }

    @Override
    public String ocrCheckIdCard(byte[] imageBytes) {
        String token = requireAccessToken();
        Map<String, byte[]> form = Map.of("ocrImage", imageBytes);
        JsonNode response = postMultipart(
                OcrVendorOperationType.OCR_CHECK,
                "ocr-check",
                properties.ocrCheckUrl(),
                form,
                token
        );
        String code = text(response, "code");
        if ("SUCCESS".equals(code)) {
            JsonNode data = response.get("data");
            if (data == null || data.isNull() || data.isEmpty()) {
                throw new ApiException(ApiCode.OCR_NO_RESULT);
            }
            try {
                return objectMapper.writeValueAsString(response);
            } catch (Exception exception) {
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
        }
        if ("OCR_NO_RESULT".equals(code)) {
            throw new ApiException(ApiCode.OCR_NO_RESULT);
        }
        throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
    }

    @Override
    public LivenessResult livenessCheck(String livenessId) {
        String token = requireAccessToken();
        Map<String, Object> requestData = Map.of(
                "livenessId", livenessId,
                "resultType", "IMAGE_BASE64"
        );
        JsonNode response = postJson(
                OcrVendorOperationType.LIVENESS_CHECK,
                "liveness-check",
                properties.livenessDetectionUrl(),
                requestData,
                token
        );
        String code = text(response, "code");
        if (!"SUCCESS".equals(code)) {
            throw new ApiException(ApiCode.OCR_LIVENESS_FAILED);
        }
        JsonNode data = response.get("data");
        int score = data.path("livenessScore").asInt(0);
        String detectionResult = text(data, "detectionResult");
        return new LivenessResult(score, detectionResult == null ? "" : detectionResult);
    }

    @Override
    public FaceCompareResult compareFaces(byte[] idCardImage, byte[] faceImage) {
        String token = requireAccessToken();
        Map<String, byte[]> form = new HashMap<>();
        form.put("firstImage", idCardImage);
        form.put("secondImage", faceImage);
        JsonNode response = postMultipart(
                OcrVendorOperationType.FACE_COMPARE,
                "face-compare",
                properties.faceRecognitionUrl(),
                form,
                token
        );
        String code = text(response, "code");
        if (!"SUCCESS".equals(code)) {
            String message = text(response, "message");
            if (isFaceAngleFailure(code, message)) {
                throw new ApiException(ApiCode.FACE_RECOGNITION_FAILED);
            }
            throw new ApiException(ApiCode.OCR_FACE_RECOGNITION_FAILED);
        }
        JsonNode data = response.get("data");
        double similarity = data.path("similarity").asDouble(0D);
        return new FaceCompareResult(similarity);
    }

    private String requireAccessToken() {
        String cached = redisTemplate.opsForValue().get(properties.tokenKeyPrefix());
        if (cached != null && !cached.isBlank()) {
            return cached;
        }
        return refreshAccessToken();
    }

    /** license-token 每次前端请求都向 Advance.ai 重新申请 access token 与 license。 */
    private String refreshAccessToken() {
        redisTemplate.delete(properties.tokenKeyPrefix());
        String token = generateToken();
        if (token == null || token.isBlank()) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
        long cacheSeconds = Math.max(60L, (long) (properties.tokenCacheSeconds() * 0.9));
        redisTemplate.opsForValue().set(properties.tokenKeyPrefix(), token, cacheSeconds, TimeUnit.SECONDS);
        return token;
    }

    private String generateToken() {
        try {
            long timestamp = System.currentTimeMillis();
            String signature = AdvanceAiHttpSupport.sha256Hex(
                    properties.accessKey() + properties.secretKey() + timestamp
            );
            Map<String, Object> requestData = Map.of(
                    "accessKey", properties.accessKey(),
                    "signature", signature,
                    "timestamp", String.valueOf(timestamp)
            );
            // Access token is infrastructure; do not write business audit rows.
            JsonNode response = postJsonInternal("access-token", properties.accessTokenUrl(), requestData, null, null);
            if (!"SUCCESS".equals(text(response, "code"))) {
                return null;
            }
            return text(response.path("data"), "token");
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
    }

    private JsonNode postJson(
            OcrVendorOperationType operationType,
            String operation,
            String pathOrUrl,
            Map<String, Object> requestData,
            String accessToken
    ) {
        return postJsonInternal(operation, pathOrUrl, requestData, accessToken, operationType);
    }

    private JsonNode postJsonInternal(
            String operation,
            String pathOrUrl,
            Map<String, Object> requestData,
            String accessToken,
            OcrVendorOperationType operationType
    ) {
        String endpoint = AdvanceAiHttpSupport.resolveEndpoint(properties, pathOrUrl);
        String mobileNo = currentMobileNo();
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestData);
        } catch (Exception exception) {
            requestJson = String.valueOf(requestData);
        }
        String sanitizedRequest = sensitiveJsonSupport.sanitizeForStorage(requestJson, mobileNo);
        String responseText = "";
        Integer httpStatus = null;
        OcrVendorCallStatus status = OcrVendorCallStatus.EXCEPTION;
        String vendorCode = null;
        String vendorMessage = null;
        String apiCode = null;
        BigDecimal score = null;
        boolean success = false;
        long vendorDurationMs = 0L;
        try {
            OcrHttpLogger.logRequest(operation, "POST", endpoint, OcrLogSupport.redactPayload(requestJson));
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("Content-Type", "application/json");
            if (accessToken != null) {
                builder.header("X-ACCESS-TOKEN", accessToken);
            }
            builder.POST(HttpRequest.BodyPublishers.ofString(requestJson));
            HttpRequest httpRequest = builder.build();
            long vendorStartedAt = System.currentTimeMillis();
            try {
                HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
                httpStatus = response.statusCode();
                responseText = response.body() == null ? "" : response.body();
            } finally {
                vendorDurationMs = System.currentTimeMillis() - vendorStartedAt;
            }
            if (httpStatus >= 400) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
                apiCode = ApiCode.OCR_SERVICE_ERROR.code();
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            JsonNode result = parseResponseBody(responseText);
            vendorCode = text(result, "code");
            vendorMessage = text(result, "message");
            score = extractScore(result);
            success = "SUCCESS".equals(vendorCode);
            status = success ? OcrVendorCallStatus.SUCCESS : OcrVendorCallStatus.VENDOR_ERROR;
            return result;
        } catch (ApiException exception) {
            apiCode = exception.apiCode().code();
            if (status == OcrVendorCallStatus.EXCEPTION) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
            }
            throw exception;
        } catch (java.net.http.HttpTimeoutException exception) {
            status = OcrVendorCallStatus.TIMEOUT;
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } catch (Exception exception) {
            status = OcrVendorCallStatus.EXCEPTION;
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } finally {
            OcrHttpLogger.logResponse(
                    operation,
                    endpoint,
                    vendorDurationMs,
                    success,
                    OcrLogSupport.redactPayload(responseText)
            );
            if (operationType != null) {
                String sanitizedResponse = sensitiveJsonSupport.sanitizeForStorage(responseText, mobileNo);
                persistCallLog(
                        operationType,
                        status,
                        apiCode,
                        vendorCode,
                        vendorMessage,
                        score,
                        null,
                        endpoint,
                        httpStatus,
                        (int) vendorDurationMs,
                        sanitizedRequest,
                        sanitizedResponse,
                        null,
                        extractEncryptedRef(sanitizedResponse, "detectionResult")
                );
            }
        }
    }

    private JsonNode postMultipart(
            OcrVendorOperationType operationType,
            String operation,
            String pathOrUrl,
            Map<String, byte[]> form,
            String accessToken
    ) {
        String endpoint = AdvanceAiHttpSupport.resolveEndpoint(properties, pathOrUrl);
        String mobileNo = currentMobileNo();
        String sanitizedRequest = sensitiveJsonSupport.describeMultipartRequest(form, mobileNo);
        String responseText = "";
        Integer httpStatus = null;
        OcrVendorCallStatus status = OcrVendorCallStatus.EXCEPTION;
        String vendorCode = null;
        String vendorMessage = null;
        String apiCode = null;
        BigDecimal score = null;
        boolean success = false;
        long vendorDurationMs = 0L;
        String idCardImageRef = firstNonBlank(
                extractEncryptedRef(sanitizedRequest, "ocrImage"),
                extractEncryptedRef(sanitizedRequest, "firstImage")
        );
        String livenessImageRef = extractEncryptedRef(sanitizedRequest, "secondImage");
        try {
            OcrHttpLogger.logRequest(operation, "POST", endpoint, OcrLogSupport.describeMultipart(form));
            AdvanceAiHttpSupport.MultipartBody multipart = AdvanceAiHttpSupport.buildMultipartBody(form);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("X-ACCESS-TOKEN", accessToken)
                    .header("Content-Type", "multipart/form-data; boundary=" + multipart.boundary())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(multipart.body()))
                    .build();
            long vendorStartedAt = System.currentTimeMillis();
            try {
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                httpStatus = response.statusCode();
                responseText = response.body() == null ? "" : response.body();
            } finally {
                vendorDurationMs = System.currentTimeMillis() - vendorStartedAt;
            }
            if (httpStatus >= 400) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
                apiCode = ApiCode.OCR_SERVICE_ERROR.code();
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            JsonNode result = parseResponseBody(responseText);
            vendorCode = text(result, "code");
            vendorMessage = text(result, "message");
            score = extractScore(result);
            success = "SUCCESS".equals(vendorCode);
            status = success ? OcrVendorCallStatus.SUCCESS : OcrVendorCallStatus.VENDOR_ERROR;
            return result;
        } catch (ApiException exception) {
            apiCode = exception.apiCode().code();
            if (status == OcrVendorCallStatus.EXCEPTION) {
                status = OcrVendorCallStatus.VENDOR_ERROR;
            }
            throw exception;
        } catch (java.net.http.HttpTimeoutException exception) {
            status = OcrVendorCallStatus.TIMEOUT;
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } catch (Exception exception) {
            status = OcrVendorCallStatus.EXCEPTION;
            apiCode = ApiCode.OCR_SERVICE_ERROR.code();
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } finally {
            OcrHttpLogger.logResponse(
                    operation,
                    endpoint,
                    vendorDurationMs,
                    success,
                    OcrLogSupport.redactPayload(responseText)
            );
            persistCallLog(
                    operationType,
                    status,
                    apiCode,
                    vendorCode,
                    vendorMessage,
                    score,
                    null,
                    endpoint,
                    httpStatus,
                    (int) vendorDurationMs,
                    sanitizedRequest,
                    sensitiveJsonSupport.sanitizeForStorage(responseText, mobileNo),
                    idCardImageRef,
                    livenessImageRef
            );
        }
    }

    private void persistCallLog(
            OcrVendorOperationType operationType,
            OcrVendorCallStatus status,
            String apiCode,
            String vendorCode,
            String vendorMessage,
            BigDecimal score,
            BigDecimal threshold,
            String endpoint,
            Integer httpStatus,
            Integer durationMs,
            String requestJson,
            String responseJson,
            String idCardImageEncryptedRef,
            String livenessImageEncryptedRef
    ) {
        OcrCallContext context = OcrCallContextHolder.get();
        Long userId = context == null ? null : context.userId();
        String partnerUserId = context == null ? null : context.partnerUserId();
        String mobileNo = context == null ? null : context.mobileNo();
        if (userId == null || mobileNo == null || mobileNo.isBlank()) {
            log.warn(
                    "OCR vendor call log missing login identity operation={} userId={} mobileNo={}",
                    operationType,
                    userId,
                    mobileNo
            );
        }
        String traceId = context == null ? null : context.traceId();
        if (traceId == null || traceId.isBlank()) {
            traceId = LogContext.traceId();
        }
        String clientRequestId = context == null ? null : context.clientRequestId();
        if (clientRequestId == null || clientRequestId.isBlank()) {
            clientRequestId = traceId;
        }
        callLogWriter.write(new OcrVendorCallLogWriter.OcrVendorCallLogEntry(
                userId,
                partnerUserId,
                mobileNo,
                operationType,
                "advanceAi",
                traceId,
                clientRequestId,
                status,
                apiCode,
                vendorCode,
                vendorMessage,
                score,
                threshold,
                endpoint,
                httpStatus,
                durationMs,
                requestJson,
                responseJson,
                idCardImageEncryptedRef,
                livenessImageEncryptedRef
        ));
    }

    private static String currentMobileNo() {
        OcrCallContext context = OcrCallContextHolder.get();
        return context == null ? null : context.mobileNo();
    }

    private String extractEncryptedRef(String json, String fieldName) {
        if (json == null || json.isBlank() || fieldName == null || fieldName.isBlank()) {
            return null;
        }
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode target = findField(root, fieldName);
            if (target == null) {
                return null;
            }
            if (target.hasNonNull("encryptedRef")) {
                String ref = target.get("encryptedRef").asText();
                return ref == null || ref.isBlank() || "store-failed".equals(ref) ? null : ref;
            }
            return null;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static JsonNode findField(JsonNode node, String fieldName) {
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isObject()) {
            if (node.has(fieldName)) {
                return node.get(fieldName);
            }
            for (JsonNode child : node) {
                JsonNode found = findField(child, fieldName);
                if (found != null) {
                    return found;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                JsonNode found = findField(child, fieldName);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    private static BigDecimal extractScore(JsonNode result) {
        if (result == null) {
            return null;
        }
        JsonNode data = result.get("data");
        if (data == null || data.isNull()) {
            return null;
        }
        if (data.has("livenessScore")) {
            return BigDecimal.valueOf(data.path("livenessScore").asDouble());
        }
        if (data.has("similarity")) {
            return BigDecimal.valueOf(data.path("similarity").asDouble());
        }
        return null;
    }

    private JsonNode parseResponseBody(String responseText) {
        if (responseText == null || responseText.isBlank()) {
            try {
                return objectMapper.readTree("{}");
            } catch (Exception exception) {
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
        }
        String trimmed = responseText.trim();
        if (trimmed.startsWith("<")) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
        try {
            return objectMapper.readTree(trimmed);
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        }
    }

    private static boolean isFaceAngleFailure(String code, String message) {
        if (code != null) {
            String normalized = code.toUpperCase();
            if (normalized.contains("FACE_ANGLE") || normalized.contains("FACE_QUALITY")) {
                return true;
            }
        }
        if (message == null || message.isBlank()) {
            return false;
        }
        String normalizedMessage = message.toLowerCase();
        return normalizedMessage.contains("face angle")
                || normalizedMessage.contains("face quality")
                || normalizedMessage.contains("front-facing");
    }

    private static String text(JsonNode node, String field) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }
}
