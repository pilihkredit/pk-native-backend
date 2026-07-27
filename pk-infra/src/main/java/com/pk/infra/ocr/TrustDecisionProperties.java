package com.pk.infra.ocr;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.trustdecision")
public class TrustDecisionProperties {
    private boolean enabled;
    private String partnerCode = "";
    private String partnerKey = "";
    private String ocrUrl = "https://id.apitd.net/verification/kyc/ocr/v1";
    private String livenessUrl = "https://id.apitd.net/verification/kyc/liveness/v1";
    private String faceComparisonUrl = "https://id.apitd.net/verification/kyc/identity/v1";
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;
    private int maxImageBytes = 3 * 1024 * 1024;
    private Duration sessionTtl = Duration.ofMinutes(30);

    public boolean enabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
    public String partnerCode() { return partnerCode; }
    public void setPartnerCode(String partnerCode) { this.partnerCode = partnerCode; }
    public String partnerKey() { return partnerKey; }
    public void setPartnerKey(String partnerKey) { this.partnerKey = partnerKey; }
    public String ocrUrl() { return ocrUrl; }
    public void setOcrUrl(String ocrUrl) { this.ocrUrl = ocrUrl; }
    public String livenessUrl() { return livenessUrl; }
    public void setLivenessUrl(String livenessUrl) { this.livenessUrl = livenessUrl; }
    public String faceComparisonUrl() { return faceComparisonUrl; }
    public void setFaceComparisonUrl(String faceComparisonUrl) { this.faceComparisonUrl = faceComparisonUrl; }
    public int connectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int readTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int maxImageBytes() { return maxImageBytes; }
    public void setMaxImageBytes(int maxImageBytes) { this.maxImageBytes = maxImageBytes; }
    public Duration sessionTtl() { return sessionTtl; }
    public void setSessionTtl(Duration sessionTtl) { this.sessionTtl = sessionTtl; }
}
