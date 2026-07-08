package com.pk.core.attribution.port;

public interface AdjustEventRecordRepository {
    long insert(AdjustEventRecordInsert record);

    void updateStatus(long id, int status, String response, String errorMessage, int retryCount);

    record AdjustEventRecordInsert(
            Long callbackEventId,
            String partnerUserId,
            Long profileId,
            String deviceUuid,
            String eventName,
            String eventToken,
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
    }
}
