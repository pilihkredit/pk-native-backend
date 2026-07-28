package com.pk.infra.callback.repository;

import com.pk.core.callback.port.LenderServerEventCallbackRepository.LenderServerEventCallbackInsert;
import java.math.BigDecimal;

public class LenderServerEventCallbackInsertParam {
    private Long id;
    private String eventId;
    private String eventType;
    private long eventTime;
    private String eventValue;
    private BigDecimal value;
    private String clientId;
    private String userId;
    private String partnerUserId;
    private String appName;
    private String countryCode;
    private String appVersion;
    private String countryName;
    private String deviceNo;
    private String systemPlatform;
    private String adId;

    public static LenderServerEventCallbackInsertParam from(LenderServerEventCallbackInsert insert) {
        LenderServerEventCallbackInsertParam param = new LenderServerEventCallbackInsertParam();
        param.eventId = insert.eventId();
        param.eventType = insert.eventType();
        param.eventTime = insert.eventTime();
        param.eventValue = insert.eventValue();
        param.value = insert.value();
        param.clientId = insert.clientId();
        param.userId = insert.userId();
        param.partnerUserId = insert.partnerUserId();
        param.appName = insert.appName();
        param.countryCode = insert.countryCode();
        param.appVersion = insert.appVersion();
        param.countryName = insert.countryName();
        param.deviceNo = insert.deviceNo();
        param.systemPlatform = insert.systemPlatform();
        param.adId = insert.adId();
        return param;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public long getEventTime() {
        return eventTime;
    }

    public String getEventValue() {
        return eventValue;
    }

    public BigDecimal getValue() {
        return value;
    }

    public String getClientId() {
        return clientId;
    }

    public String getUserId() {
        return userId;
    }

    public String getPartnerUserId() {
        return partnerUserId;
    }

    public String getAppName() {
        return appName;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getCountryName() {
        return countryName;
    }

    public String getDeviceNo() {
        return deviceNo;
    }

    public String getSystemPlatform() {
        return systemPlatform;
    }

    public String getAdId() {
        return adId;
    }
}
