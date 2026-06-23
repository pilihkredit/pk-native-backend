package com.pk.app.common.web;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.api")
public class ApiProperties {
    private String v1Prefix = ApiPaths.V1_PREFIX;

    public String v1Prefix() {
        return v1Prefix;
    }

    public void setV1Prefix(String v1Prefix) {
        this.v1Prefix = v1Prefix;
    }
}
