package com.pk.app.push.dto.response;

import java.util.List;

public record InboxMessagePageResponse(
        List<InboxMessageResponse> items,
        Long nextCursor,
        long unreadCount
) {
}
