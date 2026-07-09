package com.pk.infra.attribution.repository;

public class AdjustEventRecordInsertParam {
    private Long id;
    private Long callbackEventId;
    private String partnerUserId;
    private Long profileId;
    private String deviceUuid;
    private String eventName;
    private String appToken;
    private String idfa;
    private String idfv;
    private String gpsAdid;
    private String adid;
    private long eventTimestamp;
    private int status;
    private int retryCount;
    private String extraParams;

    public AdjustEventRecordInsertParam(
            Long callbackEventId,
            String partnerUserId,
            Long profileId,
            String deviceUuid,
            String eventName,
            String appToken,
            String idfa,
            String idfv,
            String gpsAdid,
            String adid,
            long eventTimestamp,
            int status,
            int retryCount,
            String extraParams
    ) {
        this.callbackEventId = callbackEventId;
        this.partnerUserId = partnerUserId;
        this.profileId = profileId;
        this.deviceUuid = deviceUuid;
        this.eventName = eventName;
        this.appToken = appToken;
        this.idfa = idfa;
        this.idfv = idfv;
        this.gpsAdid = gpsAdid;
        this.adid = adid;
        this.eventTimestamp = eventTimestamp;
        this.status = status;
        this.retryCount = retryCount;
        this.extraParams = extraParams;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCallbackEventId() {
        return callbackEventId;
    }

    public String getPartnerUserId() {
        return partnerUserId;
    }

    public Long getProfileId() {
        return profileId;
    }

    public String getDeviceUuid() {
        return deviceUuid;
    }

    public String getEventName() {
        return eventName;
    }

    public String getAppToken() {
        return appToken;
    }

    public String getIdfa() {
        return idfa;
    }

    public String getIdfv() {
        return idfv;
    }

    public String getGpsAdid() {
        return gpsAdid;
    }

    public String getAdid() {
        return adid;
    }

    public long getEventTimestamp() {
        return eventTimestamp;
    }

    public int getStatus() {
        return status;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public String getExtraParams() {
        return extraParams;
    }
}
