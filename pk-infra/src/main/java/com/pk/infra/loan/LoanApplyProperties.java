package com.pk.infra.loan;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.loan.apply")
public class LoanApplyProperties {
    private int maxRetries = 5;
    private long retryBackoffSeconds = 60;
    private long pollIntervalSeconds = 30;

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

    public long pollIntervalSeconds() {
        return pollIntervalSeconds;
    }

    public void setPollIntervalSeconds(long pollIntervalSeconds) {
        this.pollIntervalSeconds = pollIntervalSeconds;
    }
}
