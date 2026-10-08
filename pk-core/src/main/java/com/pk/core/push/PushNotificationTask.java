package com.pk.core.push;

import java.time.Instant;
import java.util.List;

/** Push task published from the backoffice and delivered through FCM. */
public record PushNotificationTask(
        long id,
        String taskName,
        String title,
        String body,
        String buttonText,
        String pushType,
        boolean requiredRead,
        List<String> pushPages,
        String internalUrl,
        String targetUrl,
        String externalUrl,
        String bannerUrl,
        String audienceType,
        String audienceFileName,
        List<Long> clearTargetIds,
        String notes,
        String status,
        String statusMessage,
        int totalCount,
        int successCount,
        int failureCount,
        Instant clearedAt,
        Instant publishedAt,
        Instant sendCompletedAt
) {
    public static final String TYPE_INTERNAL = "internal";
    public static final String TYPE_EXTERNAL = "external";
    public static final String TYPE_ALL = "all";
    public static final String TYPE_CLEAR_REQUIRED = "clear_required";

    public static final String STATUS_PUBLISHED = "PUBLISHED";
    public static final String STATUS_SENDING = "SENDING";
    public static final String STATUS_COMPLETED = "COMPLETED";
    public static final String STATUS_FAILED = "FAILED";

    public boolean includesInternal() {
        return TYPE_INTERNAL.equals(pushType) || TYPE_ALL.equals(pushType);
    }

    public boolean includesExternal() {
        return TYPE_EXTERNAL.equals(pushType) || TYPE_ALL.equals(pushType);
    }

    public boolean isClearRequired() {
        return TYPE_CLEAR_REQUIRED.equals(pushType);
    }
}
