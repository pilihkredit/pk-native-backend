package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductUnevenRateInsertParam {
    private Long id;
    private long repayMethodId;
    private String mobileNo;
    private int termNum;
    private BigDecimal repaymentRate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getRepayMethodId() {
        return repayMethodId;
    }

    public void setRepayMethodId(long repayMethodId) {
        this.repayMethodId = repayMethodId;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public int getTermNum() {
        return termNum;
    }

    public void setTermNum(int termNum) {
        this.termNum = termNum;
    }

    public BigDecimal getRepaymentRate() {
        return repaymentRate;
    }

    public void setRepaymentRate(BigDecimal repaymentRate) {
        this.repaymentRate = repaymentRate;
    }
}
