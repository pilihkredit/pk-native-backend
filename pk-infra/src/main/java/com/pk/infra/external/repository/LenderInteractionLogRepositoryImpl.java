package com.pk.infra.external.repository;
import com.pk.core.external.LenderInteractionLog;
import com.pk.core.external.port.LenderInteractionLogRepository;
import com.pk.infra.external.mapper.LenderInteractionLogMapper;
import org.springframework.stereotype.Repository;
@Repository public class LenderInteractionLogRepositoryImpl implements LenderInteractionLogRepository {
    private final LenderInteractionLogMapper mapper;
    public LenderInteractionLogRepositoryImpl(LenderInteractionLogMapper mapper) { this.mapper = mapper; }
    @Override public void insert(LenderInteractionLog log) { mapper.insert(log); }
}
