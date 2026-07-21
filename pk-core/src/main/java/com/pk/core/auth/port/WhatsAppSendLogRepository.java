package com.pk.core.auth.port;

import com.pk.core.auth.SmsSendResult;
import java.time.Instant;
import java.util.Optional;

public interface WhatsAppSendLogRepository {
    /** Counts successful sends ({@code provider_success = 1}) since the given instant. */
    long countSince(String mobileNo, Instant sinceInclusive);

    long insert(WhatsAppSendLogEntry entry);

    void updateProviderResult(long logId, SmsSendResult result);

    record WhatsAppSendLogEntry(
            Optional<Long> profileId,
            String mobileNo,
            String deviceNo,
            String otpToken,
            String otpCode,
            String purpose
    ) {
    }
}
