package com.pk.infra.home.mapper;
import com.pk.core.home.port.HomeLifecycleReadRepository.CreditApplySnapshot;
import com.pk.core.home.port.HomeLifecycleReadRepository.LoanApplySnapshot;
import org.apache.ibatis.annotations.Mapper; import org.apache.ibatis.annotations.Param;
@Mapper public interface HomeLifecycleReadMapper {
    CreditApplySnapshot findLatestCreditApply(@Param("profileId") long profileId);
    LoanApplySnapshot findLatestLoanApply(@Param("profileId") long profileId);
    int countPendingRepayLoans(@Param("profileId") long profileId);
    boolean hasOverdueRepay(@Param("profileId") long profileId);
    boolean hasDisbursedLoan(@Param("profileId") long profileId);
}
