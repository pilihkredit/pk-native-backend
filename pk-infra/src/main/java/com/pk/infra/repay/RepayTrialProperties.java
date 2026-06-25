package com.pk.infra.repay;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.repay.trial")
public class RepayTrialProperties {
    private Duration ttl = Duration.ofMinutes(15);

    public Duration ttl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
