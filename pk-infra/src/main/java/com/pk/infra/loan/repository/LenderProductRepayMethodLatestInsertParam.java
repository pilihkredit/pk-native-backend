package com.pk.infra.loan.repository;

public class LenderProductRepayMethodLatestInsertParam {
    private long productLatestId;
    private String repayMethod;
    private String cycleType;
    private Integer cycleInterval;
    private Integer cycleCount;
    private Integer totalCycleInterval;
    private Integer repayMethodType;
    private String unevenBillsRepaymentRateJson;
    private int sortOrder;
    private Long id;

    public long getProductLatestId() {
        return productLatestId;
    }

    public void setProductLatestId(long productLatestId) {
        this.productLatestId = productLatestId;
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

    public String getUnevenBillsRepaymentRateJson() {
        return unevenBillsRepaymentRateJson;
    }

    public void setUnevenBillsRepaymentRateJson(String unevenBillsRepaymentRateJson) {
        this.unevenBillsRepaymentRateJson = unevenBillsRepaymentRateJson;
    }

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
