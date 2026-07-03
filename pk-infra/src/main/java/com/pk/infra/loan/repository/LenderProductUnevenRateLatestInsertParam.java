package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductUnevenRateLatestInsertParam {
    private long repayMethodLatestId;
    private String mobileNo;
    private int termNum;
    private BigDecimal repaymentRate;
    private int sortOrder;

    public long getRepayMethodLatestId() {
        return repayMethodLatestId;
    }

    public void setRepayMethodLatestId(long repayMethodLatestId) {
        this.repayMethodLatestId = repayMethodLatestId;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }
}
