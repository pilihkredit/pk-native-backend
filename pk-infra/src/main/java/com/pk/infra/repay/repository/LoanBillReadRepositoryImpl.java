package com.pk.infra.repay.repository;
import com.pk.core.repay.port.LoanBillReadRepository;
import com.pk.infra.repay.mapper.LoanBillReadMapper;
import java.util.List; import java.util.Optional;
import org.springframework.stereotype.Repository;
@Repository public class LoanBillReadRepositoryImpl implements LoanBillReadRepository {
private final LoanBillReadMapper mapper; public LoanBillReadRepositoryImpl(LoanBillReadMapper mapper){this.mapper=mapper;}
@Override public List<LoanBillRecord> findByProfileIdAndBillFilter(long profileId,BillFilter filter){return mapper.findByProfileIdAndBillFilter(profileId,filter);}
@Override public Optional<LoanBillRecord> findByProfileIdAndLoanApplyId(long profileId,String loanApplyId){return Optional.ofNullable(mapper.findByProfileIdAndLoanApplyId(profileId,loanApplyId));}
@Override public List<LoanBillRecord> findPendingByProfileId(long profileId){return mapper.findPendingByProfileId(profileId);}}
