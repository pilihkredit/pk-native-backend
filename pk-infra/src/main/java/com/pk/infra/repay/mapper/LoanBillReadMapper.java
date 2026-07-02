package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.LoanBillReadRepository.BillFilter;
import com.pk.core.repay.port.LoanBillReadRepository.LoanBillRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface LoanBillReadMapper {
    List<LoanBillRecord> findByProfileIdAndBillFilter(@Param("profileId") long profileId, @Param("filter") BillFilter filter);
    LoanBillRecord findByProfileIdAndLoanApplyId(@Param("profileId") long profileId, @Param("loanApplyId") String loanApplyId);
    List<LoanBillRecord> findPendingByProfileId(@Param("profileId") long profileId);
}
