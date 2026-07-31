package com.pk.infra.outbox.repository;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.port.OutboxEventRepository;
import com.pk.infra.outbox.mapper.OutboxEventMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxEventRepositoryImpl implements OutboxEventRepository {
    private final OutboxEventMapper outboxEventMapper;

    public OutboxEventRepositoryImpl(OutboxEventMapper outboxEventMapper) {
        this.outboxEventMapper = outboxEventMapper;
    }

    @Override
    public void insertPending(OutboxEventDraft draft) {
        outboxEventMapper.insertPending(draft);
    }

    @Override
    public List<OutboxEvent> findReady(int limit) {
        return outboxEventMapper.findReady(limit);
    }

    @Override
    public void markCompleted(long id) {
        outboxEventMapper.markCompleted(id);
    }

    @Override
    public void markFailed(long id, int retryCount, Instant nextRetryAt) {
        outboxEventMapper.markFailed(id, retryCount, nextRetryAt);
    }

    @Override
    public void markTerminalFailure(long id, int retryCount) {
        outboxEventMapper.markTerminalFailure(id, retryCount);
    }
}
