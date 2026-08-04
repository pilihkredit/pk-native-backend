package com.pk.app.push.dto.response;

public record InboxMessageResponse(
        long messageId,
        String type,
        String title,
        String summary,
        String content,
        boolean read,
        long sentAt,
        String deeplink
) {
}
