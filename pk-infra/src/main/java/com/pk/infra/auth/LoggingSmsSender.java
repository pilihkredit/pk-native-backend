package com.pk.infra.auth;

import com.pk.core.auth.SmsSendResult;
import com.pk.core.auth.port.SmsSender;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingSmsSender implements SmsSender {
    private static final Logger log = LoggerFactory.getLogger(LoggingSmsSender.class);
    private static final String PROVIDER_CODE = "local";

    @Override
    public SmsSendResult send(String mobileNo, String otpCode) {
        log.info("SMS OTP dispatched to mobile {} via {} (dev profile logs code only)", mobileNo, PROVIDER_CODE);
        log.debug("OTP code for {}: {}", mobileNo, otpCode);
        return SmsSendResult.success(PROVIDER_CODE, PROVIDER_CODE + "-" + UUID.randomUUID());
    }
}
