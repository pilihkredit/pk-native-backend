package com.pk.infra.callback.repository;

import com.pk.core.callback.port.LenderServerEventCallbackRepository;
import com.pk.infra.callback.mapper.LenderServerEventCallbackMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LenderServerEventCallbackRepositoryImpl implements LenderServerEventCallbackRepository {
    private final LenderServerEventCallbackMapper mapper;

    public LenderServerEventCallbackRepositoryImpl(LenderServerEventCallbackMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public long insert(LenderServerEventCallbackInsert insert) {
        LenderServerEventCallbackInsertParam param = LenderServerEventCallbackInsertParam.from(insert);
        mapper.insert(param);
        if (param.getId() == null) {
            throw new IllegalStateException("Failed to load inserted lender server event callback id");
        }
        return param.getId();
    }

    @Override
    public Optional<LenderServerEventCallbackRecord> findByEventId(String eventId) {
        return Optional.ofNullable(mapper.findByEventId(eventId));
    }
}
