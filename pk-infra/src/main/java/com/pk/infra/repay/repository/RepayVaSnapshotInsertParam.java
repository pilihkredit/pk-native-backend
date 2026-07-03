package com.pk.infra.repay.repository;
import java.time.Instant;
public class RepayVaSnapshotInsertParam {
    private long profileId; private String snapshotNo; private String vaNo; private String bankCode; private String bankName;
    private boolean defaultFlag; private boolean disabled; private String bankChannelsJson; private Instant fetchedAt;
    private String lastLenderRequestJson; private String lastLenderResponseJson;
    public RepayVaSnapshotInsertParam(long profileId,String snapshotNo,String vaNo,String bankCode,String bankName,boolean defaultFlag,boolean disabled,String bankChannelsJson,String lastLenderRequestJson,String lastLenderResponseJson,Instant fetchedAt){
        this.profileId=profileId;this.snapshotNo=snapshotNo;this.vaNo=vaNo;this.bankCode=bankCode;this.bankName=bankName;
        this.defaultFlag=defaultFlag;this.disabled=disabled;this.bankChannelsJson=bankChannelsJson;
        this.lastLenderRequestJson=lastLenderRequestJson;this.lastLenderResponseJson=lastLenderResponseJson;this.fetchedAt=fetchedAt;}
    public long getProfileId(){return profileId;} public String getSnapshotNo(){return snapshotNo;} public String getVaNo(){return vaNo;}
    public String getBankCode(){return bankCode;} public String getBankName(){return bankName;}
    public boolean isDefaultFlag(){return defaultFlag;} public boolean isDisabled(){return disabled;}
    public String getBankChannelsJson(){return bankChannelsJson;} public Instant getFetchedAt(){return fetchedAt;}
    public String getLastLenderRequestJson(){return lastLenderRequestJson;}
    public String getLastLenderResponseJson(){return lastLenderResponseJson;}
}
