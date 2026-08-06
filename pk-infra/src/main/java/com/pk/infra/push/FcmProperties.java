package com.pk.infra.push;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.fcm")
public class FcmProperties {
    private boolean enabled;
    private String projectId;
    /** Absolute/relative path to Firebase service-account JSON. */
    private String credentialsPath;
    /** Inline service-account JSON (takes precedence over credentialsPath when non-blank). */
    private String credentialsJson;

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

    public boolean configured() {
        return projectId != null && !projectId.isBlank()
                && ((credentialsJson != null && !credentialsJson.isBlank())
                || (credentialsPath != null && !credentialsPath.isBlank()));
    }
}
