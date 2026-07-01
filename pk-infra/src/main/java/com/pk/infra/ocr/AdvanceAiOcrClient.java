package com.pk.infra.ocr;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.profile.port.AdvanceAiOcrPort;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.StringRedisTemplate;

public class AdvanceAiOcrClient implements AdvanceAiOcrPort {
    private final OcrProperties properties;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;

    public AdvanceAiOcrClient(
            OcrProperties properties,
            StringRedisTemplate redisTemplate,
            ObjectMapper objectMapper
    ) {
        this.properties = properties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(properties.connectTimeoutMs()))
                .build();
    }

    @Override
    public LicenseTokenResult getLicenseToken(Long licenseEffectiveSeconds) {
        long effectiveSeconds = licenseEffectiveSeconds == null || licenseEffectiveSeconds <= 0
                ? properties.licenseEffectiveSeconds()
                : licenseEffectiveSeconds;
        String token = requireAccessToken();
        Map<String, Object> requestData = Map.of("licenseEffectiveSeconds", effectiveSeconds);
        JsonNode response = postJson("license-token", properties.licenseUrl(), requestData, token);
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
        JsonNode response = postMultipart("ocr-check", properties.ocrCheckUrl(), form, token);
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
        JsonNode response = postJson("liveness-check", properties.livenessDetectionUrl(), requestData, token);
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
        JsonNode response = postMultipart("face-compare", properties.faceRecognitionUrl(), form, token);
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
            JsonNode response = postJson("access-token", properties.accessTokenUrl(), requestData, null);
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

    private JsonNode postJson(String operation, String pathOrUrl, Map<String, Object> requestData, String accessToken) {
        String endpoint = AdvanceAiHttpSupport.resolveEndpoint(properties, pathOrUrl);
        String requestJson;
        try {
            requestJson = objectMapper.writeValueAsString(requestData);
        } catch (Exception exception) {
            requestJson = String.valueOf(requestData);
        }
        String sanitizedRequest = OcrLogSupport.redactPayload(requestJson);
        long startedAt = System.currentTimeMillis();
        String responseText = "";
        boolean success = false;
        try {
            OcrHttpLogger.logRequest(operation, "POST", endpoint, sanitizedRequest);
            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(Duration.ofMillis(properties.readTimeoutMs()))
                    .header("Content-Type", "application/json");
            if (accessToken != null) {
                builder.header("X-ACCESS-TOKEN", accessToken);
            }
            builder.POST(HttpRequest.BodyPublishers.ofString(requestJson));
            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            responseText = response.body() == null ? "" : response.body();
            if (response.statusCode() >= 400) {
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            JsonNode result = parseResponseBody(responseText);
            success = "SUCCESS".equals(text(result, "code"));
            return result;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            OcrHttpLogger.logResponse(
                    operation,
                    endpoint,
                    durationMs,
                    success,
                    OcrLogSupport.redactPayload(responseText)
            );
        }
    }

    private JsonNode postMultipart(String operation, String pathOrUrl, Map<String, byte[]> form, String accessToken) {
        String endpoint = AdvanceAiHttpSupport.resolveEndpoint(properties, pathOrUrl);
        long startedAt = System.currentTimeMillis();
        String responseText = "";
        boolean success = false;
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
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            responseText = response.body() == null ? "" : response.body();
            if (response.statusCode() >= 400) {
                throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
            }
            JsonNode result = parseResponseBody(responseText);
            success = "SUCCESS".equals(text(result, "code"));
            return result;
        } catch (ApiException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ApiException(ApiCode.OCR_SERVICE_ERROR);
        } finally {
            long durationMs = System.currentTimeMillis() - startedAt;
            OcrHttpLogger.logResponse(
                    operation,
                    endpoint,
                    durationMs,
                    success,
                    OcrLogSupport.redactPayload(responseText)
            );
        }
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
