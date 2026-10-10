package com.pk.core.push.port;

import com.pk.core.push.PushNotificationTask;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PushNotificationTaskRepository {

    Optional<PushNotificationTask> findById(long id);

    /** Tasks waiting for the delivery worker, oldest publish first. */
    List<PushNotificationTask> findReadyToDeliver(int limit);

    /** Claims a PUBLISHED task for delivery; false when another worker took it. */
    boolean tryMarkSending(long id);

    void markCompleted(long id, int totalCount, int successCount, int failureCount, Instant completedAt);

    void markFailed(long id, String statusMessage, Instant failedAt);

    /** Marks cleared target tasks of a clear_required push; returns updated row count. */
    int markCleared(List<Long> taskIds, Instant clearedAt);
}
