package com.pk.core.credit.port;

import java.util.List;

public interface ProfileVersionRepository {
    long createSnapshot(long profileId, String mobileNo, List<String> completedModules, String source);
}
