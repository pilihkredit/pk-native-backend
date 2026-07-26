package com.pk.infra.loan.repository;

import java.time.Instant;

public class LenderProductListInsertParam {
    private Long id;
    private long userId;
    private long creditApplicationId;
    private String applyId;
    private String creditApplyNo;
    private String lenderUserId;
    private String creditStatus;
    private String productStatus;
    private String contentHash;
    private Long externalInteractionId;
    private Instant fetchedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getCreditApplicationId() {
        return creditApplicationId;
    }

    public void setCreditApplicationId(long creditApplicationId) {
        this.creditApplicationId = creditApplicationId;
    }

    public String getApplyId() {
        return applyId;
    }

    public void setApplyId(String applyId) {
        this.applyId = applyId;
    }

    public String getCreditApplyNo() {
        return creditApplyNo;
    }

    public void setCreditApplyNo(String creditApplyNo) {
        this.creditApplyNo = creditApplyNo;
    }

    public String getLenderUserId() {
        return lenderUserId;
    }

    public void setLenderUserId(String lenderUserId) {
        this.lenderUserId = lenderUserId;
    }

    public String getCreditStatus() {
        return creditStatus;
    }

    public void setCreditStatus(String creditStatus) {
        this.creditStatus = creditStatus;
    }

    public String getProductStatus() {
        return productStatus;
    }

    public void setProductStatus(String productStatus) {
        this.productStatus = productStatus;
    }

    public String getContentHash() {
        return contentHash;
    }

    public void setContentHash(String contentHash) {
        this.contentHash = contentHash;
    }

    public Long getExternalInteractionId() {
        return externalInteractionId;
    }

    public void setExternalInteractionId(Long externalInteractionId) {
        this.externalInteractionId = externalInteractionId;
    }

    public Instant getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(Instant fetchedAt) {
        this.fetchedAt = fetchedAt;
    }
}
