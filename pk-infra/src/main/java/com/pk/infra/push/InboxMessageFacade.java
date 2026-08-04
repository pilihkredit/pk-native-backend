package com.pk.infra.push;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.InboxMessage;
import com.pk.core.push.InboxMessagePage;
import com.pk.core.push.port.InboxMessageRepository;
import java.util.List;

public class InboxMessageFacade {
    private static final int MAX_PAGE_SIZE = 50;
    private final InboxMessageRepository inboxMessageRepository;

    public InboxMessageFacade(InboxMessageRepository inboxMessageRepository) {
        this.inboxMessageRepository = inboxMessageRepository;
    }

    public long unreadCount(long userId) {
        requireUserId(userId);
        return inboxMessageRepository.countUnread(userId);
    }

    public InboxMessagePage findPage(long userId, Long cursor, int pageSize) {
        requireUserId(userId);
        if (cursor != null && cursor <= 0L) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        int resolvedPageSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
        List<InboxMessage> records = inboxMessageRepository.findPage(userId, cursor, resolvedPageSize + 1);
        boolean hasMore = records.size() > resolvedPageSize;
        List<InboxMessage> items = hasMore ? records.subList(0, resolvedPageSize) : records;
        Long nextCursor = hasMore ? items.get(items.size() - 1).messageId() : null;
        return new InboxMessagePage(items, nextCursor, inboxMessageRepository.countUnread(userId));
    }

    public InboxMessage findDetail(long userId, long messageId) {
        requireUserId(userId);
        if (messageId <= 0L) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        return inboxMessageRepository.findByUserIdAndMessageId(userId, messageId)
                .orElseThrow(() -> new ApiException(ApiCode.INBOX_MESSAGE_NOT_FOUND));
    }

    public void markRead(long userId, long messageId) {
        requireUserId(userId);
        if (messageId <= 0L) {
            throw new ApiException(ApiCode.INVALID_REQUEST_PARAMETERS);
        }
        inboxMessageRepository.markRead(userId, messageId);
    }

    private static void requireUserId(long userId) {
        if (userId <= 0L) {
            throw new ApiException(ApiCode.UNAUTHORIZED_REQUEST);
        }
    }
}
