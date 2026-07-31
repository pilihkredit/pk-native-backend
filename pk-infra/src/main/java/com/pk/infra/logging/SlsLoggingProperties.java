package com.pk.infra.logging;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.logging.sls")
public class SlsLoggingProperties {
    private boolean enabled;
    private String endpoint = "";
    private String project = "";
    private String logstore = "";
    private String accessKeyId = "";
    private String accessKeySecret = "";
    private String topic = "";
    private String source = "";

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String endpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String project() {
        return project;
    }

    public void setProject(String project) {
        this.project = project;
    }

    public String logstore() {
        return logstore;
    }

    public void setLogstore(String logstore) {
        this.logstore = logstore;
    }

    public String accessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String accessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String topic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public String source() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public boolean credentialsConfigured() {
        return endpoint != null && !endpoint.isBlank()
                && project != null && !project.isBlank()
                && logstore != null && !logstore.isBlank()
                && accessKeyId != null && !accessKeyId.isBlank()
                && accessKeySecret != null && !accessKeySecret.isBlank();
    }
}
