package com.pk.infra.loan.repository;

import com.pk.core.loan.port.LoanLenderStatusQueryRepository;
import com.pk.infra.loan.mapper.LoanLenderStatusQueryMapper;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LoanLenderStatusQueryRepositoryImpl implements LoanLenderStatusQueryRepository {
    private final LoanLenderStatusQueryMapper mapper;

    public LoanLenderStatusQueryRepositoryImpl(LoanLenderStatusQueryMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public void upsert(LoanLenderStatusQueryData data) {
        mapper.upsert(data);
    }

    @Override
    public Optional<LoanLenderStatusQueryData> findByLoanApplyId(String loanApplyId) {
        return Optional.ofNullable(mapper.findByLoanApplyId(loanApplyId));
    }

    @Override
    public Optional<LoanLenderStatusQueryData> findByLoanApplyIdAndProfileId(String loanApplyId, long profileId) {
        return Optional.ofNullable(mapper.findByLoanApplyIdAndProfileId(loanApplyId, profileId));
    }
}
