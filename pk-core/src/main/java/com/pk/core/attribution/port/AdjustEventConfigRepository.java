package com.pk.core.attribution.port;

import java.util.Optional;

public interface AdjustEventConfigRepository {
    Optional<AdjustEventConfigData> findEnabledByEventNameAndAppToken(String eventName, String appToken);

    record AdjustEventConfigData(
            long id,
            String eventName,
            String appToken,
            boolean enabled
    ) {
    }
}
