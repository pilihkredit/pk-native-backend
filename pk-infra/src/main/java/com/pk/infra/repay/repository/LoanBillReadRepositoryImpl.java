package com.pk.infra.repay.repository;

import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.infra.repay.mapper.LoanBillReadMapper;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class LoanBillReadRepositoryImpl implements LoanBillReadRepository {
    private final LoanBillReadMapper mapper;

    public LoanBillReadRepositoryImpl(LoanBillReadMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public List<LoanBillRecord> findByUserIdAndBillFilter(long userId, BillFilter filter) {
        return mapper.findByUserIdAndBillFilter(userId, filter);
    }

    @Override
    public Optional<LoanBillRecord> findByUserIdAndLoanApplyId(long userId, String loanApplyId) {
        return Optional.ofNullable(mapper.findByUserIdAndLoanApplyId(userId, loanApplyId));
    }

    @Override
    public List<LoanBillRecord> findPendingByUserId(long userId) {
        return mapper.findPendingByUserId(userId);
    }

    @Override
    public List<TrialBackfillCandidate> findDueForTrialBackfill(
            long afterLoanApplicationId,
            int limit,
            Instant createdFromInclusive
    ) {
        return mapper.findDueForTrialBackfill(afterLoanApplicationId, limit, createdFromInclusive);
    }
}
