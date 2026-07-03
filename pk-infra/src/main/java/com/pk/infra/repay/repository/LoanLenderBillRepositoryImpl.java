package com.pk.infra.repay.repository;

import com.pk.core.repay.port.LoanLenderBillRepository;
import com.pk.infra.repay.mapper.LoanLenderBillMapper;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class LoanLenderBillRepositoryImpl implements LoanLenderBillRepository {
    private final LoanLenderBillMapper mapper;

    public LoanLenderBillRepositoryImpl(LoanLenderBillMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void upsert(LoanLenderBillData data) {
        mapper.upsert(data);
    }

    @Override
    public List<LoanLenderBillData> findByProfileIdAndBillStatuses(long profileId, List<String> billStatuses) {
        return mapper.findByProfileIdAndBillStatuses(profileId, billStatuses);
    }
}
