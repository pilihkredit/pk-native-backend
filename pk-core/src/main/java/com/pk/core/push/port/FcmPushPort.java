package com.pk.core.push.port;

import java.util.Map;

/** Sends a data/notification push via FCM HTTP v1. */
public interface FcmPushPort {
    FcmSendResult send(FcmSendCommand command);

    record FcmSendCommand(
            String fcmToken,
            String title,
            String body,
            Map<String, String> data
    ) {
    }

    record FcmSendResult(
            boolean success,
            String messageName,
            String rawBody
    ) {
    }
}
