package com.pk.infra.loan.repository;
import com.pk.core.loan.port.LoanQuoteRepository.LoanQuoteTermInsert;
import java.math.BigDecimal; import java.time.Instant;
public class LoanQuoteTermInsertParam {
    private long quoteId; private String mobileNo; private int termNo; private Instant dueDate; private BigDecimal schdAmount;
    private BigDecimal schdPrincipal; private BigDecimal schdInterest; private String feeJson;
    public static LoanQuoteTermInsertParam from(long quoteId, LoanQuoteTermInsert t){LoanQuoteTermInsertParam p=new LoanQuoteTermInsertParam();
    p.quoteId=quoteId;p.mobileNo=t.mobileNo();p.termNo=t.termNo();p.dueDate=t.dueDate();p.schdAmount=t.schdAmount();
    p.schdPrincipal=t.schdPrincipal();p.schdInterest=t.schdInterest();p.feeJson=t.feeJson();return p;}
    public long getQuoteId(){return quoteId;} public String getMobileNo(){return mobileNo;}
    public int getTermNo(){return termNo;} public Instant getDueDate(){return dueDate;}
    public BigDecimal getSchdAmount(){return schdAmount;} public BigDecimal getSchdPrincipal(){return schdPrincipal;}
    public BigDecimal getSchdInterest(){return schdInterest;} public String getFeeJson(){return feeJson;}
}
