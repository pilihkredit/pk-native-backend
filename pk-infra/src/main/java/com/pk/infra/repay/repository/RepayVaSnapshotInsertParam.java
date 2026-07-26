package com.pk.infra.repay.repository;

import java.time.Instant;

public class RepayVaSnapshotInsertParam {
    private long userId; private String snapshotNo; private String vaNo; private String bankCode; private String bankName;
    private boolean defaultFlag; private boolean disabled; private String bankChannelsJson;
    private Long externalInteractionId; private Instant fetchedAt;
    public RepayVaSnapshotInsertParam(long userId,String snapshotNo,String vaNo,String bankCode,String bankName,boolean defaultFlag,boolean disabled,String bankChannelsJson,Long externalInteractionId,Instant fetchedAt){
        this.userId=userId;this.snapshotNo=snapshotNo;this.vaNo=vaNo;this.bankCode=bankCode;this.bankName=bankName;
        this.defaultFlag=defaultFlag;this.disabled=disabled;this.bankChannelsJson=bankChannelsJson;
        this.externalInteractionId=externalInteractionId;this.fetchedAt=fetchedAt;}
    public long getUserId(){return userId;}
    public String getSnapshotNo(){return snapshotNo;}
    public String getVaNo(){return vaNo;}
    public String getBankCode(){return bankCode;}
    public String getBankName(){return bankName;}
    public boolean isDefaultFlag(){return defaultFlag;}
    public boolean isDisabled(){return disabled;}
    public String getBankChannelsJson(){return bankChannelsJson;}
    public Long getExternalInteractionId(){return externalInteractionId;}
    public Instant getFetchedAt(){return fetchedAt;}
}
