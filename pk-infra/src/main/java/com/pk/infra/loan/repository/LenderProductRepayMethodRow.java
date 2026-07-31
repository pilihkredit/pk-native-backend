package com.pk.infra.loan.repository;

public class LenderProductRepayMethodRow {
    private long id;
    private long productId;
    private String repayMethod;
    private String cycleType;
    private Integer cycleInterval;
    private Integer cycleCount;
    private Integer totalCycleInterval;
    private Integer repayMethodType;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public String getRepayMethod() {
        return repayMethod;
    }

    public void setRepayMethod(String repayMethod) {
        this.repayMethod = repayMethod;
    }

    public String getCycleType() {
        return cycleType;
    }

    public void setCycleType(String cycleType) {
        this.cycleType = cycleType;
    }

    public Integer getCycleInterval() {
        return cycleInterval;
    }

    public void setCycleInterval(Integer cycleInterval) {
        this.cycleInterval = cycleInterval;
    }

    public Integer getCycleCount() {
        return cycleCount;
    }

    public void setCycleCount(Integer cycleCount) {
        this.cycleCount = cycleCount;
    }

    public Integer getTotalCycleInterval() {
        return totalCycleInterval;
    }

    public void setTotalCycleInterval(Integer totalCycleInterval) {
        this.totalCycleInterval = totalCycleInterval;
    }

    public Integer getRepayMethodType() {
        return repayMethodType;
    }

    public void setRepayMethodType(Integer repayMethodType) {
        this.repayMethodType = repayMethodType;
    }
}
