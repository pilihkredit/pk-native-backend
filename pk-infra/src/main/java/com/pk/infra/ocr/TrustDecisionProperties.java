package com.pk.infra.ocr;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.trustdecision")
public class TrustDecisionProperties {
    private boolean enabled;
    private String partnerCode = "";
    private String partnerKey = "";
    private String ocrUrl = "https://id.apitd.net/verification/kyc/ocr/v1";
    private String livenessLicenseUrl = "https://id-credit.apitd.net/verification/kyc/sdk/liveness/license/v1";
    private String livenessResultUrl = "https://id-credit.apitd.net/verification/kyc/sdk/liveness/result/v1";
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
    public String livenessLicenseUrl() { return livenessLicenseUrl; }
    public void setLivenessLicenseUrl(String value) { this.livenessLicenseUrl = value; }
    public String livenessResultUrl() { return livenessResultUrl; }
    public void setLivenessResultUrl(String value) { this.livenessResultUrl = value; }
    public int connectTimeoutMs() { return connectTimeoutMs; }
    public void setConnectTimeoutMs(int connectTimeoutMs) { this.connectTimeoutMs = connectTimeoutMs; }
    public int readTimeoutMs() { return readTimeoutMs; }
    public void setReadTimeoutMs(int readTimeoutMs) { this.readTimeoutMs = readTimeoutMs; }
    public int maxImageBytes() { return maxImageBytes; }
    public void setMaxImageBytes(int maxImageBytes) { this.maxImageBytes = maxImageBytes; }
    public Duration sessionTtl() { return sessionTtl; }
    public void setSessionTtl(Duration sessionTtl) { this.sessionTtl = sessionTtl; }

    public void validateEnabledSettings() {
        if (!enabled) {
            return;
        }
        if (partnerCode == null || partnerCode.isBlank() || partnerKey == null || partnerKey.isBlank()) {
            throw new IllegalStateException(
                    "pk.trustdecision.partner-code and partner-key are required when TrustDecision is enabled");
        }
    }
}
