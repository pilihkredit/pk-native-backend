package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanApplicationRepository;
import com.pk.infra.loan.mapper.LoanApplicationMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository
public class LoanApplicationRepositoryImpl implements LoanApplicationRepository {
    private final LoanApplicationMapper mapper;
    public LoanApplicationRepositoryImpl(LoanApplicationMapper mapper) { this.mapper = mapper; }
    @Override public Optional<LoanApplicationRecord> findById(long id) { return Optional.ofNullable(mapper.findById(id)); }
    @Override public Optional<LoanApplicationRecord> findByRequestId(String requestId) { return Optional.ofNullable(mapper.findByRequestId(requestId)); }
    @Override public Optional<LoanApplicationRecord> findByLoanApplyId(String loanApplyId) { return Optional.ofNullable(mapper.findByLoanApplyId(loanApplyId)); }
    @Override public Optional<LoanApplicationRecord> findByLoanApplyIdAndUserId(String loanApplyId, long userId) {
        return Optional.ofNullable(mapper.findByLoanApplyIdAndUserId(loanApplyId, userId));
    }
    @Override public long insert(LoanApplicationInsert insert) {
        mapper.insert(LoanApplicationInsertParam.from(insert));
        Long id = mapper.findIdByLoanApplyId(insert.loanApplyId());
        if (id == null) throw new IllegalStateException("Failed to load inserted loan application");
        return id;
    }
    @Override public void updateStatus(long id, String status, String externalStatus) { mapper.updateStatus(id, status, externalStatus); }
    @Override public void markSubmitted(long id, String externalLoanApplyNo, String externalStatus) { mapper.markSubmitted(id, externalLoanApplyNo, externalStatus); }
    @Override public void markLenderApplySubmitted(LenderApplySubmitted submitted) {
        mapper.markLenderApplySubmitted(LenderApplySubmittedParam.from(submitted));
    }
    @Override public void updateDisbursementDetails(long id, String billNo, BigDecimal applyAmt, BigDecimal payAmount, Instant payTime) {
        mapper.updateDisbursementDetails(id, billNo, applyAmt, payAmount, payTime);
    }
    @Override public void scheduleNextPoll(long id, Instant nextPollAt) { mapper.scheduleNextPoll(id, nextPollAt); }
    @Override public List<LoanApplicationRecord> findPendingPoll(int limit) { return mapper.findPendingPoll(limit); }
}
