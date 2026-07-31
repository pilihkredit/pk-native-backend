package com.pk.infra.reference.repository;
import java.time.Instant;
public class RefAreaUpsertParam {
    private String areaCode; private String areaName; private String parentCode; private int level;
    private String status; private Instant syncedAt;
    public RefAreaUpsertParam(String areaCode,String areaName,String parentCode,int level,String status,Instant syncedAt){
        this.areaCode=areaCode;this.areaName=areaName;this.parentCode=parentCode;this.level=level;this.status=status;this.syncedAt=syncedAt;}
    public String getAreaCode(){return areaCode;} public String getAreaName(){return areaName;}
    public String getParentCode(){return parentCode;} public int getLevel(){return level;}
    public String getStatus(){return status;} public Instant getSyncedAt(){return syncedAt;}
}
