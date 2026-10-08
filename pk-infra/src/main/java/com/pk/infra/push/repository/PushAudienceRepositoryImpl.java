package com.pk.infra.push.repository;

import com.pk.core.push.port.PushAudienceRepository;
import com.pk.infra.push.mapper.PushAudienceMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class PushAudienceRepositoryImpl implements PushAudienceRepository {
    private final PushAudienceMapper mapper;

    public PushAudienceRepositoryImpl(PushAudienceMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<Long> findUserIdsByTaskId(long taskId) {
        return mapper.findUserIdsByTaskId(taskId);
    }
}
