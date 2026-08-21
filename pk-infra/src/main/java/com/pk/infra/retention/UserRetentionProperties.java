package com.pk.infra.retention;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.user.retention")
public class UserRetentionProperties {
    private int batchSize = 50;
    private int defaultYears = 5;
    private String zoneId = "Asia/Jakarta";

    public int batchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int defaultYears() {
        return defaultYears;
    }

    public void setDefaultYears(int defaultYears) {
        this.defaultYears = defaultYears;
    }

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
