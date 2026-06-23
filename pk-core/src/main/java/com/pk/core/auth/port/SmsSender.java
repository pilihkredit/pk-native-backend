package com.pk.core.auth.port;

import com.pk.core.auth.SmsSendResult;

public interface SmsSender {
    SmsSendResult send(String mobileNo, String otpCode);
}
