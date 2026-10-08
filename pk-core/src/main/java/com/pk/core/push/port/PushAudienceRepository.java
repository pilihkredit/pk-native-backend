package com.pk.core.push.port;

import java.util.List;

public interface PushAudienceRepository {

    /** Distinct resolved user ids of an uploaded audience task. */
    List<Long> findUserIdsByTaskId(long taskId);
}
