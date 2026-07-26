package com.pk.infra.loan.repository;

import java.math.BigDecimal;

public class LenderProductUnevenRateRow {
    private long id;
    private long repayMethodId;
    private int termNum;
    private BigDecimal repaymentRate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getRepayMethodId() {
        return repayMethodId;
    }

    public void setRepayMethodId(long repayMethodId) {
        this.repayMethodId = repayMethodId;
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
