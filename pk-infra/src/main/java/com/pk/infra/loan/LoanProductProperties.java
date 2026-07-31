package com.pk.infra.loan;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.loan.product")
public class LoanProductProperties {
    private Duration cacheTtl = Duration.ofSeconds(30);

    public Duration cacheTtl() {
        return cacheTtl;
    }

    public void setCacheTtl(Duration cacheTtl) {
        this.cacheTtl = cacheTtl;
    }
}
