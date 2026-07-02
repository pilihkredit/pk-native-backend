package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialOrderInsert;
import java.math.BigDecimal;
public class RepaymentTrialOrderInsertParam {
    private long trialId; private long loanApplicationId; private String loanApplyId; private boolean settle;
    private String termNosJson; private BigDecimal shouldAmount; private String billStatus; private String termInfoJson;
    public static RepaymentTrialOrderInsertParam from(long trialId,TrialOrderInsert o){RepaymentTrialOrderInsertParam p=new RepaymentTrialOrderInsertParam();
    p.trialId=trialId;p.loanApplicationId=o.loanApplicationId();p.loanApplyId=o.loanApplyId();p.settle=o.settle();
    p.termNosJson=o.termNosJson();p.shouldAmount=o.shouldAmount();p.billStatus=o.billStatus();p.termInfoJson=o.termInfoJson();return p;}
    public long getTrialId(){return trialId;} public long getLoanApplicationId(){return loanApplicationId;}
    public String getLoanApplyId(){return loanApplyId;} public boolean isSettle(){return settle;}
    public String getTermNosJson(){return termNosJson;} public BigDecimal getShouldAmount(){return shouldAmount;}
    public String getBillStatus(){return billStatus;} public String getTermInfoJson(){return termInfoJson;}
}
