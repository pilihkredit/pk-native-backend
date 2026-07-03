package com.pk.infra.loan.mapper;
import com.pk.core.loan.port.LoanApplicationRepository.LoanApplicationRecord;
import com.pk.infra.loan.repository.LenderApplySubmittedParam;
import com.pk.infra.loan.repository.LoanApplicationInsertParam;
import java.time.Instant;
import java.math.BigDecimal;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
@Mapper
public interface LoanApplicationMapper {
    LoanApplicationRecord findById(@Param("id") long id);
    LoanApplicationRecord findByRequestId(@Param("requestId") String requestId);
    LoanApplicationRecord findByLoanApplyId(@Param("loanApplyId") String loanApplyId);
    LoanApplicationRecord findByLoanApplyIdAndProfileId(@Param("loanApplyId") String loanApplyId, @Param("profileId") long profileId);
    int insert(LoanApplicationInsertParam param);
    Long findIdByLoanApplyId(@Param("loanApplyId") String loanApplyId);
    int updateStatus(@Param("id") long id, @Param("status") String status, @Param("externalStatus") String externalStatus);
    int markSubmitted(@Param("id") long id, @Param("externalLoanApplyNo") String externalLoanApplyNo, @Param("externalStatus") String externalStatus);
    int markLenderApplySubmitted(LenderApplySubmittedParam param);
    int updateDisbursementDetails(@Param("id") long id, @Param("billNo") String billNo, @Param("applyAmt") BigDecimal applyAmt,
        @Param("payAmount") BigDecimal payAmount, @Param("payTime") Instant payTime);
    int scheduleNextPoll(@Param("id") long id, @Param("nextPollAt") Instant nextPollAt);
    List<LoanApplicationRecord> findPendingPoll(@Param("limit") int limit);
}
