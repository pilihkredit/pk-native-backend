package com.pk.infra.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.profile.sync")
public class ProfileSyncProperties {
    public static final String MODE_INLINE = "inline";
    public static final String MODE_OUTBOX = "outbox";

    private String mode = MODE_INLINE;
    private int maxRetries = 5;
    private long retryBackoffSeconds = 60;

    public String mode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean inlineEnabled() {
        return MODE_INLINE.equalsIgnoreCase(mode);
    }

    public int maxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public long retryBackoffSeconds() {
        return retryBackoffSeconds;
    }

    public void setRetryBackoffSeconds(long retryBackoffSeconds) {
        this.retryBackoffSeconds = retryBackoffSeconds;
    }
}
