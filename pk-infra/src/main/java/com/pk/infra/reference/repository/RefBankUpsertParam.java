package com.pk.infra.reference.repository;
import com.pk.core.reference.BankReference;
import java.time.Instant;
public class RefBankUpsertParam {
    private String bankCode; private String bankName; private Integer bankType; private String iconUrl;
    private String status; private Instant syncedAt;
    public RefBankUpsertParam(BankReference bank,String status,Instant syncedAt){
        bankCode=bank.bankCode();bankName=bank.bankName();bankType=bank.bankType();iconUrl=bank.iconUrl();this.status=status;this.syncedAt=syncedAt;}
    public String getBankCode(){return bankCode;} public String getBankName(){return bankName;}
    public Integer getBankType(){return bankType;} public String getIconUrl(){return iconUrl;}
    public String getStatus(){return status;} public Instant getSyncedAt(){return syncedAt;}
}
