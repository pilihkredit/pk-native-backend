package com.pk.infra.external.repository;

import com.pk.core.external.ExternalInteractionCallbackLog;
import com.pk.core.external.port.ExternalInteractionCallbackLogRepository;
import com.pk.infra.external.mapper.ExternalInteractionCallbackLogMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ExternalInteractionCallbackLogRepositoryImpl implements ExternalInteractionCallbackLogRepository {
    private final ExternalInteractionCallbackLogMapper mapper;

    public ExternalInteractionCallbackLogRepositoryImpl(ExternalInteractionCallbackLogMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long insert(ExternalInteractionCallbackLog log) {
        mapper.insert(log);
        Long id = log.getId();
        if (id == null) {
            throw new IllegalStateException("external_interaction_callback insert did not return generated id");
        }
        return id;
    }

    @Override
    public Optional<Long> findIdByIdempotencyKey(String idempotencyKey) {
        return Optional.ofNullable(mapper.findIdByIdempotencyKey(idempotencyKey));
    }

    @Override
    public void updateResponse(
            long id,
            String mobileNo,
            String responseCode,
            String responseMsg,
            String responseRef,
            boolean success,
            int durationMs
    ) {
        mapper.updateResponse(id, mobileNo, responseCode, responseMsg, responseRef, success, durationMs);
    }
}
