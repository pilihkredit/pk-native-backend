package com.pk.core.attribution.port;

import java.util.Optional;

public interface AppConfRepository {
    Optional<String> findConfigValue(String configKey);
}
