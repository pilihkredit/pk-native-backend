package com.pk.core.push.port;

import com.pk.core.push.InboxMessage;
import java.util.List;
import java.util.Optional;

public interface InboxMessageRepository {
    long countUnread(long userId);

    List<InboxMessage> findPage(long userId, Long cursor, int limit);

    Optional<InboxMessage> findByUserIdAndMessageId(long userId, long messageId);

    void markRead(long userId, long messageId);
}
