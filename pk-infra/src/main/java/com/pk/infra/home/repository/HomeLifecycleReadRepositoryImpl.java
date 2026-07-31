package com.pk.infra.home.repository;
import com.pk.core.home.port.HomeLifecycleReadRepository;
import com.pk.infra.home.mapper.HomeLifecycleReadMapper;
import java.util.Optional; import org.springframework.stereotype.Repository;
@Repository public class HomeLifecycleReadRepositoryImpl implements HomeLifecycleReadRepository {
private final HomeLifecycleReadMapper mapper; public HomeLifecycleReadRepositoryImpl(HomeLifecycleReadMapper mapper){this.mapper=mapper;}
@Override public Optional<CreditApplySnapshot> findLatestCreditApply(long userId){return Optional.ofNullable(mapper.findLatestCreditApply(userId));}
@Override public Optional<LoanApplySnapshot> findLatestLoanApply(long userId){return Optional.ofNullable(mapper.findLatestLoanApply(userId));}
@Override public int countPendingRepayLoans(long userId){return mapper.countPendingRepayLoans(userId);}
@Override public boolean hasOverdueRepay(long userId){return mapper.hasOverdueRepay(userId);}
@Override public boolean hasDisbursedLoan(long userId){return mapper.hasDisbursedLoan(userId);}}
