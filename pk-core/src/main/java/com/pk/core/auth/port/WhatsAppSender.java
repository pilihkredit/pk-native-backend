package com.pk.core.auth.port;

import com.pk.core.auth.SmsSendResult;

public interface WhatsAppSender {
    SmsSendResult send(String mobileNo, String otpCode);
}
