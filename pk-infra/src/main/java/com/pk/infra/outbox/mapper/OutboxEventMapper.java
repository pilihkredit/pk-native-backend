package com.pk.infra.outbox.mapper;

import com.pk.core.outbox.OutboxEvent;
import com.pk.core.outbox.port.OutboxEventRepository.OutboxEventDraft;
import java.time.Instant;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OutboxEventMapper {
    int insertPending(OutboxEventDraft draft);

    List<OutboxEvent> findReady(@Param("limit") int limit);

    int markCompleted(@Param("id") long id);

    int markFailed(
            @Param("id") long id,
            @Param("retryCount") int retryCount,
            @Param("nextRetryAt") Instant nextRetryAt
    );

    int markTerminalFailure(@Param("id") long id, @Param("retryCount") int retryCount);
}
