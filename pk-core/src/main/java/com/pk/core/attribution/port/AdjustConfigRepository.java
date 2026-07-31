package com.pk.core.attribution.port;

import java.util.Optional;

public interface AdjustConfigRepository {
    Optional<AdjustConfigData> findActiveByOsName(String osName);

    record AdjustConfigData(
            long id,
            String appToken,
            String apiToken,
            String baseUrl,
            int timeout,
            String osName
    ) {
    }
}
