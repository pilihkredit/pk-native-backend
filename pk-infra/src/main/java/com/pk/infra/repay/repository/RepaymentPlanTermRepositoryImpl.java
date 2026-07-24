package com.pk.infra.repay.repository;

import com.pk.core.repay.port.RepaymentPlanTermRepository;
import com.pk.infra.repay.mapper.RepaymentPlanTermMapper;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class RepaymentPlanTermRepositoryImpl implements RepaymentPlanTermRepository {
    private final RepaymentPlanTermMapper mapper;
    public RepaymentPlanTermRepositoryImpl(RepaymentPlanTermMapper mapper){this.mapper=mapper;}
    @Override @Transactional public void upsertTerms(long loanApplicationId,String loanApplyId,String billNo,List<TermUpsert> terms,Long externalInteractionId,Instant syncedAt){
        for(TermUpsert term:terms) mapper.upsertTerm(RepaymentPlanTermUpsertParam.of(loanApplicationId,loanApplyId,billNo,term,externalInteractionId,syncedAt));}
    @Override public List<TermRecord> findByLoanApplicationId(long loanApplicationId){return mapper.findByLoanApplicationId(loanApplicationId);}
    @Override public List<TermRecord> findPendingByProfileId(long profileId){return mapper.findPendingByProfileId(profileId);}
}
