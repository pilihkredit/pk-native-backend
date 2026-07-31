package com.pk.app.debug.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.debug.user-progress")
public class DebugUserProgressProperties {
    private boolean enabled;
    private String token;

    public boolean enabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String token() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean tokenConfigured() {
        return token != null && !token.isBlank();
    }
}
