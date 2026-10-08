package com.pk.infra.push.repository;

import java.time.Instant;

/** Flat row of push_task; JSON columns stay as raw strings for the repository to decode. */
public record PushNotificationTaskRow(
        long id,
        String taskName,
        String title,
        String body,
        String buttonText,
        String pushType,
        int requiredRead,
        String pushPagesJson,
        String internalUrl,
        String targetUrl,
        String externalUrl,
        String bannerUrl,
        String audienceType,
        String audienceFileName,
        String clearTargetIdsJson,
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
}
