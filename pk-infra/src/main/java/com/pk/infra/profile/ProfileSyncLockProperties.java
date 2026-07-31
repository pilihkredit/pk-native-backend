package com.pk.infra.profile;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.profile.sync.lock")
public class ProfileSyncLockProperties {
    private long waitTimeoutMs = 35_000;
    private long leaseTimeoutMs = 60_000;
    private long retryIntervalMs = 100;

    public long waitTimeoutMs() {
        return waitTimeoutMs;
    }

    public void setWaitTimeoutMs(long waitTimeoutMs) {
        this.waitTimeoutMs = waitTimeoutMs;
    }

    public long leaseTimeoutMs() {
        return leaseTimeoutMs;
    }

    public void setLeaseTimeoutMs(long leaseTimeoutMs) {
        this.leaseTimeoutMs = leaseTimeoutMs;
    }

    public long retryIntervalMs() {
        return retryIntervalMs;
    }

    public void setRetryIntervalMs(long retryIntervalMs) {
        this.retryIntervalMs = retryIntervalMs;
    }
}
