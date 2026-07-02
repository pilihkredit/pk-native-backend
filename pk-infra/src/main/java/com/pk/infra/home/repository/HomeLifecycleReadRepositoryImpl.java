package com.pk.infra.home.repository;
import com.pk.core.home.port.HomeLifecycleReadRepository;
import com.pk.infra.home.mapper.HomeLifecycleReadMapper;
import java.util.Optional; import org.springframework.stereotype.Repository;
@Repository public class HomeLifecycleReadRepositoryImpl implements HomeLifecycleReadRepository {
private final HomeLifecycleReadMapper mapper; public HomeLifecycleReadRepositoryImpl(HomeLifecycleReadMapper mapper){this.mapper=mapper;}
@Override public Optional<CreditApplySnapshot> findLatestCreditApply(long profileId){return Optional.ofNullable(mapper.findLatestCreditApply(profileId));}
@Override public Optional<LoanApplySnapshot> findLatestLoanApply(long profileId){return Optional.ofNullable(mapper.findLatestLoanApply(profileId));}
@Override public int countPendingRepayLoans(long profileId){return mapper.countPendingRepayLoans(profileId);}
@Override public boolean hasOverdueRepay(long profileId){return mapper.hasOverdueRepay(profileId);}
@Override public boolean hasDisbursedLoan(long profileId){return mapper.hasDisbursedLoan(profileId);}}
