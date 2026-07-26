package com.pk.infra.home.mapper;
import com.pk.core.home.port.HomeLifecycleReadRepository.CreditApplySnapshot;
import com.pk.core.home.port.HomeLifecycleReadRepository.LoanApplySnapshot;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface HomeLifecycleReadMapper {
    CreditApplySnapshot findLatestCreditApply(@Param("userId") long userId);
    LoanApplySnapshot findLatestLoanApply(@Param("userId") long userId);
    int countPendingRepayLoans(@Param("userId") long userId);
    boolean hasOverdueRepay(@Param("userId") long userId);
    boolean hasDisbursedLoan(@Param("userId") long userId);
}
