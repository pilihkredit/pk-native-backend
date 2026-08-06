package com.pk.infra.push;

import com.pk.core.api.ApiCode;
import com.pk.core.api.ApiException;
import com.pk.core.push.port.FcmPushPort;

public class DisabledFcmPushAdapter implements FcmPushPort {
    @Override
    public FcmSendResult send(FcmSendCommand command) {
        throw new ApiException(ApiCode.SERVICE_UNAVAILABLE);
    }
}
