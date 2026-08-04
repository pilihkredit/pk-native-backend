package com.pk.infra.push.repository;

import java.time.Instant;

public record InboxMessageRow(
        long messageId,
        String type,
        String title,
        String summary,
        String content,
        Instant readAt,
        Instant sentAt,
        String deeplink
) {
}
