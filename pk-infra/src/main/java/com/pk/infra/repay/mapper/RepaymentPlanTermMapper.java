package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.RepaymentPlanTermRepository.TermRecord;
import com.pk.infra.repay.repository.RepaymentPlanTermUpsertParam;
import java.time.Instant; import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface RepaymentPlanTermMapper {
    int upsertTerm(RepaymentPlanTermUpsertParam param);
    List<TermRecord> findByLoanApplicationId(@Param("loanApplicationId") long loanApplicationId);
    List<TermRecord> findPendingByUserId(@Param("userId") long userId);
}
