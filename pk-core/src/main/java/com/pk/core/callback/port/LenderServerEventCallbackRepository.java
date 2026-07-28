package com.pk.core.callback.port;

import java.math.BigDecimal;
import java.util.Optional;

public interface LenderServerEventCallbackRepository {
    long insert(LenderServerEventCallbackInsert insert);

    Optional<LenderServerEventCallbackRecord> findByEventId(String eventId);

    record LenderServerEventCallbackInsert(
            String eventId,
            String eventType,
            long eventTime,
            String eventValue,
            BigDecimal value,
            String clientId,
            String userId,
            String partnerUserId,
            String appName,
            String countryCode,
            String appVersion,
            String countryName,
            String deviceNo,
            String systemPlatform,
            String adId
    ) {
    }

    record LenderServerEventCallbackRecord(
            long id,
            String eventId,
            String eventType
    ) {
    }
}
