package com.pk.core.callback.port;

import java.math.BigDecimal;

public interface ServerEventCallbackParser {
    ParsedServerEventCallback parse(String rawPayloadJson);

    record ParsedServerEventCallback(
            String eventId,
            String eventType,
            Long eventTime,
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
}
