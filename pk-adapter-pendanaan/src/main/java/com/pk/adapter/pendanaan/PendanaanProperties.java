package com.pk.adapter.pendanaan;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.lender.pendanaan")
public class PendanaanProperties {
    public static final String MODE_FAKE = "fake";
    public static final String MODE_HTTP = "http";

    private String mode = MODE_FAKE;
    private String baseUrl = "";
    private String clientId = "";
    private String clientSecret = "";
    private String appName = "";
    private int connectTimeoutMs = 10_000;
    private int readTimeoutMs = 30_000;

    public String mode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String baseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String clientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String clientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String appName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public boolean httpEnabled() {
        return MODE_HTTP.equalsIgnoreCase(mode);
    }

    public boolean httpCredentialsPresent() {
        return isPresent(baseUrl)
                && isPresent(clientId)
                && isPresent(clientSecret)
                && isPresent(appName);
    }

    public void validateHttpSettings() {
        if (!httpEnabled()) {
            return;
        }
        if (!httpCredentialsPresent()) {
            throw new IllegalStateException(
                    "pk.lender.pendanaan.base-url, client-id, client-secret, and app-name are required when mode=http"
            );
        }
    }

    private static boolean isPresent(String value) {
        return value != null && !value.isBlank();
    }
}
