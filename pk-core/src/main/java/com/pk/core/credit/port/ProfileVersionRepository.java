package com.pk.core.credit.port;

import java.util.List;

public interface ProfileVersionRepository {
    long createSnapshot(long userId, String mobileNo, List<String> completedModules, String source);
}
