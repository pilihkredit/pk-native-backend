package com.pk.infra.push;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.port.FcmPushPort;

public class DisabledFcmPushAdapter implements FcmPushPort {
    @Override
    public FcmSendResult send(FcmSendCommand command) {
        throw new ApiException(
                ApiCode.SERVICE_UNAVAILABLE,
                "FCM is disabled or credentials missing; set pk.fcm.enabled=true and PK_FCM_CREDENTIALS_JSON or PK_FCM_CREDENTIALS_PATH"
        );
    }
}
