package com.pk.infra.ocr;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.ocr")
public class OcrProperties {
    private boolean enabled = false;
    private String accessKey = "";
    private String secretKey = "";
    /** Full URL, e.g. https://api.advance.ai/openapi/auth/ticket/v1/generate-token */
    private String accessTokenUrl = "https://api.advance.ai/openapi/auth/ticket/v1/generate-token";
    private String baseUrl = "https://api.advance.ai/openapi";
    private String licenseUrl = "/liveness/v1/auth-license";
    private String ocrCheckUrl = "/ocr/v1/check";
    private String livenessDetectionUrl = "/liveness/v1/detect";
    private String faceRecognitionUrl = "/face/v1/compare";
    private String tokenKeyPrefix = "pk:ocr:token";
    private long tokenCacheSeconds = 3600L;
    private long licenseEffectiveSeconds = 86400L;
    private int livenessThreshold = 60;
    private int faceThreshold = 60;
    private int maxImageBytes = 2 * 1024 * 1024;
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;
    private Duration sessionTtl = Duration.ofMinutes(30);

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String accessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String secretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String accessTokenUrl() {
        return accessTokenUrl;
    }

    public void setAccessTokenUrl(String accessTokenUrl) {
        this.accessTokenUrl = accessTokenUrl;
    }

    public String licenseUrl() {
        return licenseUrl;
    }

    public void setLicenseUrl(String licenseUrl) {
        this.licenseUrl = licenseUrl;
    }

    public String ocrCheckUrl() {
        return ocrCheckUrl;
    }

    public void setOcrCheckUrl(String ocrCheckUrl) {
        this.ocrCheckUrl = ocrCheckUrl;
    }

    public String livenessDetectionUrl() {
        return livenessDetectionUrl;
    }

    public void setLivenessDetectionUrl(String livenessDetectionUrl) {
        this.livenessDetectionUrl = livenessDetectionUrl;
    }

    public String faceRecognitionUrl() {
        return faceRecognitionUrl;
    }

    public void setFaceRecognitionUrl(String faceRecognitionUrl) {
        this.faceRecognitionUrl = faceRecognitionUrl;
    }

    public String tokenKeyPrefix() {
        return tokenKeyPrefix;
    }

    public void setTokenKeyPrefix(String tokenKeyPrefix) {
        this.tokenKeyPrefix = tokenKeyPrefix;
    }

    public long tokenCacheSeconds() {
        return tokenCacheSeconds;
    }

    public void setTokenCacheSeconds(long tokenCacheSeconds) {
        this.tokenCacheSeconds = tokenCacheSeconds;
    }

    public long licenseEffectiveSeconds() {
        return licenseEffectiveSeconds;
    }

    public void setLicenseEffectiveSeconds(long licenseEffectiveSeconds) {
        this.licenseEffectiveSeconds = licenseEffectiveSeconds;
    }

    public int livenessThreshold() {
        return livenessThreshold;
    }

    public void setLivenessThreshold(int livenessThreshold) {
        this.livenessThreshold = livenessThreshold;
    }

    public int faceThreshold() {
        return faceThreshold;
    }

    public void setFaceThreshold(int faceThreshold) {
        this.faceThreshold = faceThreshold;
    }

    public int maxImageBytes() {
        return maxImageBytes;
    }

    public void setMaxImageBytes(int maxImageBytes) {
        this.maxImageBytes = maxImageBytes;
    }

    public int connectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int readTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public Duration sessionTtl() {
        return sessionTtl;
    }

    public void setSessionTtl(Duration sessionTtl) {
        this.sessionTtl = sessionTtl;
    }
}
