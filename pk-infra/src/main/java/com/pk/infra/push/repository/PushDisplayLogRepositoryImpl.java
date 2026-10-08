package com.pk.infra.push.repository;

import com.pk.core.push.port.PushDisplayLogRepository;
import com.pk.infra.push.mapper.PushDisplayLogMapper;
import java.time.Instant;
import org.springframework.stereotype.Repository;

@Repository
public class PushDisplayLogRepositoryImpl implements PushDisplayLogRepository {
    private final PushDisplayLogMapper mapper;

    public PushDisplayLogRepositoryImpl(PushDisplayLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean insertDisplayedIfAbsent(long userId, long taskId, Instant displayedAt) {
        return mapper.insertDisplayedIfAbsent(userId, taskId, displayedAt) > 0;
    }
}
