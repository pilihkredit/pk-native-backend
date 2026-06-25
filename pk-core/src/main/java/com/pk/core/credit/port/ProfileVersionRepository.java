package com.pk.core.credit.port;

import java.util.List;

public interface ProfileVersionRepository {
    long createSnapshot(long profileId, List<String> completedModules, String source);
}
