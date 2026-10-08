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

    /**
     * Mandatory in-app popups the user has not displayed yet, oldest publish first:
     * internal/all + required_read, not cleared, no display log, audience matched.
     */
    List<PushNotificationTask> findPendingRequiredForUser(long userId, int limit);

    /** Marks cleared target tasks of a clear_required push; returns updated row count. */
    int markCleared(List<Long> taskIds, Instant clearedAt);
}
