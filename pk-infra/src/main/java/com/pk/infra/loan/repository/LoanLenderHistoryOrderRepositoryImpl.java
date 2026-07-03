package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanLenderHistoryOrderRepository;
import com.pk.infra.loan.mapper.LoanLenderHistoryOrderMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class LoanLenderHistoryOrderRepositoryImpl implements LoanLenderHistoryOrderRepository {
    private final LoanLenderHistoryOrderMapper mapper;

    public LoanLenderHistoryOrderRepositoryImpl(LoanLenderHistoryOrderMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void upsert(LoanLenderHistoryOrderData data) {
        mapper.upsert(data);
    }

    @Override
    public List<LoanLenderHistoryOrderData> findByProfileId(long profileId) {
        return mapper.findByProfileId(profileId);
    }
}
