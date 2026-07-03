package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanQuoteRepository.LoanQuoteInsert;
import java.math.BigDecimal; import java.time.Instant;
public class LoanQuoteInsertParam {
    private long id; private String quoteNo; private long creditApplicationId; private Long productSnapshotId;
    private String mobileNo; private String productCode; private String repayMethod; private BigDecimal applyAmt; private BigDecimal loanPrincipal;
    private BigDecimal payAmount; private BigDecimal schdAmount; private BigDecimal interest; private Integer totalDays;
    private String feeJson; private String lastLenderRequestJson; private String lastLenderResponseJson; private String rawResponseJson; private Instant quotedAt;
    public static LoanQuoteInsertParam from(LoanQuoteInsert c){LoanQuoteInsertParam p=new LoanQuoteInsertParam();
    p.quoteNo=c.quoteNo();p.creditApplicationId=c.creditApplicationId();p.mobileNo=c.mobileNo();p.productSnapshotId=c.productSnapshotId();
    p.productCode=c.productCode();p.repayMethod=c.repayMethod();p.applyAmt=c.applyAmt();p.loanPrincipal=c.loanPrincipal();
    p.payAmount=c.payAmount();p.schdAmount=c.schdAmount();p.interest=c.interest();p.totalDays=c.totalDays();
    p.feeJson=c.feeJson();p.lastLenderRequestJson=c.lastLenderRequestJson();p.lastLenderResponseJson=c.lastLenderResponseJson();
    p.rawResponseJson=c.rawResponseJson();p.quotedAt=c.quotedAt();return p;}
    public long getId(){return id;} public void setId(long id){this.id=id;}
    public String getQuoteNo(){return quoteNo;} public long getCreditApplicationId(){return creditApplicationId;}
    public String getMobileNo(){return mobileNo;}
    public Long getProductSnapshotId(){return productSnapshotId;} public String getProductCode(){return productCode;}
    public String getRepayMethod(){return repayMethod;} public BigDecimal getApplyAmt(){return applyAmt;}
    public BigDecimal getLoanPrincipal(){return loanPrincipal;} public BigDecimal getPayAmount(){return payAmount;}
    public BigDecimal getSchdAmount(){return schdAmount;} public BigDecimal getInterest(){return interest;}
    public Integer getTotalDays(){return totalDays;} public String getFeeJson(){return feeJson;}
    public String getLastLenderRequestJson(){return lastLenderRequestJson;}
    public String getLastLenderResponseJson(){return lastLenderResponseJson;}
    public String getRawResponseJson(){return rawResponseJson;} public Instant getQuotedAt(){return quotedAt;}
}
