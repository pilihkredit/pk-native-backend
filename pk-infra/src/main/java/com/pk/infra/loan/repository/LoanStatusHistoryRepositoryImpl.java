package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanStatusHistoryRepository;
import com.pk.infra.loan.mapper.LoanStatusHistoryMapper;
import org.springframework.stereotype.Repository;
@Repository public class LoanStatusHistoryRepositoryImpl implements LoanStatusHistoryRepository {
    private final LoanStatusHistoryMapper mapper;
    public LoanStatusHistoryRepositoryImpl(LoanStatusHistoryMapper mapper) { this.mapper = mapper; }
    @Override public void insert(long loanApplicationId, String fromStatus, String toStatus, String externalStatus, String source) {
        mapper.insert(loanApplicationId, fromStatus, toStatus, externalStatus, source);
    }
}
