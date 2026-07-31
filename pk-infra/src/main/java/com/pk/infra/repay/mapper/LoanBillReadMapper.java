package com.pk.infra.repay.mapper;
import com.pk.core.repay.port.LoanBillReadRepository.BillFilter;
import com.pk.core.repay.port.LoanBillReadRepository.LoanBillRecord;
import java.util.List;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface LoanBillReadMapper {
    List<LoanBillRecord> findByUserIdAndBillFilter(@Param("userId") long userId, @Param("filter") BillFilter filter);
    LoanBillRecord findByUserIdAndLoanApplyId(@Param("userId") long userId, @Param("loanApplyId") String loanApplyId);
    List<LoanBillRecord> findPendingByUserId(@Param("userId") long userId);
}
