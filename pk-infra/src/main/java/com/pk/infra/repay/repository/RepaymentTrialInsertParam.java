package com.pk.infra.repay.repository;
import com.pk.core.repay.port.RepaymentTrialSnapshotRepository.TrialSnapshotInsert;
import java.math.BigDecimal;
public class RepaymentTrialInsertParam {
    private long id; private String trialNo; private long profileId; private String trialType;
    private Integer totalBillCount; private BigDecimal totalShouldAmount; private BigDecimal totalReductionAmount;
    private String defaultVaJson; private String rawResponseJson;
    public static RepaymentTrialInsertParam from(TrialSnapshotInsert i){RepaymentTrialInsertParam p=new RepaymentTrialInsertParam();
    p.trialNo=i.trialNo();p.profileId=i.profileId();p.trialType=i.trialType();p.totalBillCount=i.totalBillCount();
    p.totalShouldAmount=i.totalShouldAmount();p.totalReductionAmount=i.totalReductionAmount();
    p.defaultVaJson=i.defaultVaJson();p.rawResponseJson=i.rawResponseJson();return p;}
    public long getId(){return id;} public void setId(long id){this.id=id;}
    public String getTrialNo(){return trialNo;} public long getProfileId(){return profileId;}
    public String getTrialType(){return trialType;} public Integer getTotalBillCount(){return totalBillCount;}
    public BigDecimal getTotalShouldAmount(){return totalShouldAmount;} public BigDecimal getTotalReductionAmount(){return totalReductionAmount;}
    public String getDefaultVaJson(){return defaultVaJson;} public String getRawResponseJson(){return rawResponseJson;}
}
