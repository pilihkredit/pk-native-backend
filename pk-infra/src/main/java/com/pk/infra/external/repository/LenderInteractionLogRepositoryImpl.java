package com.pk.infra.external.repository;

import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.infra.external.mapper.LenderInteractionLogMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class LenderInteractionLogRepositoryImpl implements LenderInteractionLogRepository {
    private final LenderInteractionLogMapper mapper;

    public LenderInteractionLogRepositoryImpl(LenderInteractionLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long insert(LenderInteractionLog log) {
        mapper.insert(log);
        Long id = log.getId();
        if (id == null) {
            throw new IllegalStateException("external_interaction insert did not return generated id");
        }
        return id;
    }
}
