package com.pk.infra.credit;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.credit.status-backfill")
public class CreditStatusBackfillProperties {
    private int batchSize = 50;

    public int batchSize() {
        return batchSize;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }
}
