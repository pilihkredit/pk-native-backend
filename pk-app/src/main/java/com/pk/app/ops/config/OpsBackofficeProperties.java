package com.pk.app.ops.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.ops.backoffice")
public class OpsBackofficeProperties {
    private boolean enabled;
    private String token = "";
    private List<String> allowedClientIps = new ArrayList<>();

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
        this.token = token == null ? "" : token;
    }

    public List<String> allowedClientIps() {
        return allowedClientIps;
    }

    public void setAllowedClientIps(List<String> allowedClientIps) {
        this.allowedClientIps = allowedClientIps == null ? new ArrayList<>() : allowedClientIps;
    }

    public boolean tokenConfigured() {
        return token != null && !token.isBlank();
    }

    public boolean ipWhitelistConfigured() {
        return allowedClientIps != null
                && allowedClientIps.stream().anyMatch(ip -> ip != null && !ip.isBlank());
    }
}
