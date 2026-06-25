package com.pk.infra.reference;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "pk.reference")
public class ReferenceProperties {
    private Duration bankCacheTtl = Duration.ofHours(24);
    private Duration areaCacheTtl = Duration.ofHours(24);

    public Duration bankCacheTtl() {
        return bankCacheTtl;
    }

    public void setBankCacheTtl(Duration bankCacheTtl) {
        this.bankCacheTtl = bankCacheTtl;
    }

    public Duration areaCacheTtl() {
        return areaCacheTtl;
    }

    public void setAreaCacheTtl(Duration areaCacheTtl) {
        this.areaCacheTtl = areaCacheTtl;
    }
}
