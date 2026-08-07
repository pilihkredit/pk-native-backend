package com.pk.infra.credit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.credit.status-backfill")
public class CreditStatusBackfillProperties {
    private int batchSize = 50;
    /**
     * Calendar days to include based on credit_application.created_at.
     * 1 = from start of today in {@link #zoneId} (default).
     * {@code <= 0} = no time window (all history).
     */
    private int lookbackDays = 1;
    private String zoneId = "Asia/Jakarta";

    public int batchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public int lookbackDays() {
        return lookbackDays;
    }

    public void setLookbackDays(int lookbackDays) {
        this.lookbackDays = lookbackDays;
    }

    public String zoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }
}
