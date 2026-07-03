package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepaymentPlanTermRepository.TermUpsert;
import java.math.BigDecimal; import java.time.Instant;
public class RepaymentPlanTermUpsertParam {
    private long loanApplicationId; private String loanApplyId; private String billNo; private String subBillNo;
    private int termNo; private String termStatus; private Instant dueDate; private Instant graceDate;
    private BigDecimal schdAmount; private BigDecimal shouldAmount; private BigDecimal paidAmount;
    private int overdueDays; private String amountDetailJson; private Instant lastRepayTime; private Instant syncedAt;
    private String lastLenderRequestJson; private String lastLenderResponseJson;
    public static RepaymentPlanTermUpsertParam of(long loanApplicationId,String loanApplyId,String billNo,TermUpsert term,String lastLenderRequestJson,String lastLenderResponseJson,Instant syncedAt){
        RepaymentPlanTermUpsertParam p=new RepaymentPlanTermUpsertParam();
        p.loanApplicationId=loanApplicationId;p.loanApplyId=loanApplyId;p.billNo=billNo;p.subBillNo=term.subBillNo();
        p.termNo=term.termNo();p.termStatus=term.termStatus();p.dueDate=term.dueDate();p.graceDate=term.graceDate();
        p.schdAmount=term.schdAmount();p.shouldAmount=term.shouldAmount();p.paidAmount=term.paidAmount();
        p.overdueDays=term.overdueDays();p.amountDetailJson=term.amountDetailJson();p.lastRepayTime=term.lastRepayTime();p.syncedAt=syncedAt;
        p.lastLenderRequestJson=lastLenderRequestJson;p.lastLenderResponseJson=lastLenderResponseJson;return p;}
    public long getLoanApplicationId(){return loanApplicationId;} public String getLoanApplyId(){return loanApplyId;}
    public String getBillNo(){return billNo;} public String getSubBillNo(){return subBillNo;} public int getTermNo(){return termNo;}
    public String getTermStatus(){return termStatus;} public Instant getDueDate(){return dueDate;} public Instant getGraceDate(){return graceDate;}
    public BigDecimal getSchdAmount(){return schdAmount;} public BigDecimal getShouldAmount(){return shouldAmount;}
    public BigDecimal getPaidAmount(){return paidAmount;} public int getOverdueDays(){return overdueDays;}
    public String getAmountDetailJson(){return amountDetailJson;} public Instant getLastRepayTime(){return lastRepayTime;}
    public Instant getSyncedAt(){return syncedAt;}
    public String getLastLenderRequestJson(){return lastLenderRequestJson;}
    public String getLastLenderResponseJson(){return lastLenderResponseJson;}
}
