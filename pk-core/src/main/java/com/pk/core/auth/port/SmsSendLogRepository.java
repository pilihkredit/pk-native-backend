package com.pk.core.auth.port;

import com.pk.core.auth.SmsSendResult;
import java.time.Instant;
import java.util.Optional;

public interface SmsSendLogRepository {
    long countSince(String mobileNo, Instant sinceInclusive);

    long insert(SmsSendLogEntry entry);

    void updateProviderResult(long logId, SmsSendResult result);

    record SmsSendLogEntry(
            Optional<Long> profileId,
            String mobileNo,
            String deviceNo,
            String otpToken,
            String otpCode,
            String purpose
    ) {
    }
}
