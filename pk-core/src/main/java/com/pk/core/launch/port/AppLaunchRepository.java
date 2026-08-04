package com.pk.core.launch.port;

import com.pk.core.launch.AppLaunchRecord;
import java.time.Instant;

public interface AppLaunchRepository {
    boolean record(AppLaunchRecord record);

    long countEvents(Instant fromInclusive, Instant toExclusive, String deviceNo);
}
