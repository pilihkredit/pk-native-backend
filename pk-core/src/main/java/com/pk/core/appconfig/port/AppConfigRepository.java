package com.pk.core.appconfig.port;

import java.util.Optional;

public interface AppConfigRepository {
    Optional<AppConfigRecord> findByKey(String key);

    record AppConfigRecord(long id, String key, String valueJson) {
    }
}
