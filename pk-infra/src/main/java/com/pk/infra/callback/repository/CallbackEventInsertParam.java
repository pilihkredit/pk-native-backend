package com.pk.infra.callback.repository;
import com.pk.core.callback.port.CallbackEventRepository.CallbackEventInsert;
import java.time.Instant;
public class CallbackEventInsertParam {
    private String callbackNo; private String providerCode; private String callbackType; private String businessId;
    private String idempotencyKey; private String externalStatus; private String payloadJson;
    private String processStatus; private Instant receivedAt;
    public static CallbackEventInsertParam from(CallbackEventInsert i){CallbackEventInsertParam p=new CallbackEventInsertParam();
    p.callbackNo=i.callbackNo();p.providerCode=i.providerCode();p.callbackType=i.callbackType();p.businessId=i.businessId();
    p.idempotencyKey=i.idempotencyKey();p.externalStatus=i.externalStatus();p.payloadJson=i.payloadJson();
    p.processStatus=i.processStatus();p.receivedAt=i.receivedAt();return p;}
    public String getCallbackNo(){return callbackNo;} public String getProviderCode(){return providerCode;}
    public String getCallbackType(){return callbackType;} public String getBusinessId(){return businessId;}
    public String getIdempotencyKey(){return idempotencyKey;} public String getExternalStatus(){return externalStatus;}
    public String getPayloadJson(){return payloadJson;} public String getProcessStatus(){return processStatus;}
    public Instant getReceivedAt(){return receivedAt;}
}
