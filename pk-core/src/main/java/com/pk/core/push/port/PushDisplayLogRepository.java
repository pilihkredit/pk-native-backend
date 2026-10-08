package com.pk.core.push.port;

import java.time.Instant;

public interface PushDisplayLogRepository {

    /** Records a popup display once; false when this user already reported it. */
    boolean insertDisplayedIfAbsent(long userId, long taskId, Instant displayedAt);
}
