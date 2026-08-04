package com.pk.infra.push.repository;

import com.pk.core.push.InboxMessage;
import com.pk.core.push.port.InboxMessageRepository;
import com.pk.infra.push.mapper.InboxMessageMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class InboxMessageRepositoryImpl implements InboxMessageRepository {
    private final InboxMessageMapper inboxMessageMapper;

    public InboxMessageRepositoryImpl(InboxMessageMapper inboxMessageMapper) {
        this.inboxMessageMapper = inboxMessageMapper;
    }

    @Override
    public long countUnread(long userId) {
        return inboxMessageMapper.countUnread(userId);
    }

    @Override
    public List<InboxMessage> findPage(long userId, Long cursor, int limit) {
        return inboxMessageMapper.findPage(userId, cursor, limit).stream().map(this::toMessage).toList();
    }

    @Override
    public Optional<InboxMessage> findByUserIdAndMessageId(long userId, long messageId) {
        return Optional.ofNullable(inboxMessageMapper.findByUserIdAndMessageId(userId, messageId))
                .map(this::toMessage);
    }

    @Override
    public void markRead(long userId, long messageId) {
        inboxMessageMapper.markRead(userId, messageId);
    }

    private InboxMessage toMessage(InboxMessageRow row) {
        return new InboxMessage(
                row.messageId(),
                row.type(),
                row.title(),
                row.summary(),
                row.content(),
                row.readAt() != null,
                row.sentAt(),
                row.deeplink()
        );
    }
}
