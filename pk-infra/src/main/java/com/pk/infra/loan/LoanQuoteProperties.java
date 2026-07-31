package com.pk.infra.loan;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.loan.quote")
public class LoanQuoteProperties {
    private Duration ttl = Duration.ofMinutes(15);

    public Duration ttl() {
        return ttl;
    }

    public void setTtl(Duration ttl) {
        this.ttl = ttl;
    }
}
