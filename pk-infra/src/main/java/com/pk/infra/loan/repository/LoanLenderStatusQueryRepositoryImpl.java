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
    public void insert(LoanLenderStatusQueryData data) {
        mapper.insert(data);
    }

    @Override
    public Optional<LoanLenderStatusQueryData> findLatestByLoanApplyId(String loanApplyId) {
        return Optional.ofNullable(mapper.findLatestByLoanApplyId(loanApplyId));
    }

    @Override
    public Optional<LoanLenderStatusQueryData> findLatestByLoanApplyIdAndProfileId(String loanApplyId, long profileId) {
        return Optional.ofNullable(mapper.findLatestByLoanApplyIdAndProfileId(loanApplyId, profileId));
    }
}
