package com.pk.infra.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.fcm")
public class FcmProperties {
    private boolean enabled;
    private String projectId;
    /** Absolute/relative path to Firebase service-account JSON. */
    private String credentialsPath;
    /** Inline service-account JSON. Prefer base64 or path in deploy environments. */
    private String credentialsJson;
    /** Base64 of the service-account JSON (avoids YAML/env truncation of raw JSON). */
    private String credentialsJsonBase64;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String projectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String credentialsPath() {
        return credentialsPath;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    public String credentialsJson() {
        return credentialsJson;
    }

    public void setCredentialsJson(String credentialsJson) {
        this.credentialsJson = credentialsJson;
    }

    public String credentialsJsonBase64() {
        return credentialsJsonBase64;
    }

    public void setCredentialsJsonBase64(String credentialsJsonBase64) {
        this.credentialsJsonBase64 = credentialsJsonBase64;
    }

    public boolean configured() {
        return projectId != null && !projectId.isBlank()
                && ((credentialsPath != null && !credentialsPath.isBlank())
                || (credentialsJsonBase64 != null && !credentialsJsonBase64.isBlank())
                || (credentialsJson != null && !credentialsJson.isBlank()));
    }
}
