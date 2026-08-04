package com.pk.core.push;

import java.util.List;

public record InboxMessagePage(
        List<InboxMessage> items,
        Long nextCursor,
        long unreadCount
) {
}
