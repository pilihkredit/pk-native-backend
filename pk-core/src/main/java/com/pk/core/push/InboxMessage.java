package com.pk.core.push;

import java.time.Instant;

public record InboxMessage(
        long messageId,
        String type,
        String title,
        String summary,
        String content,
        boolean read,
        Instant sentAt,
        String deeplink
) {
}
