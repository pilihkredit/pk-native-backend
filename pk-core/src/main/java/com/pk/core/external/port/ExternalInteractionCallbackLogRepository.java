package com.pk.core.external.port;

import com.pk.core.external.ExternalInteractionCallbackLog;
import java.util.Optional;

public interface ExternalInteractionCallbackLogRepository {
    long insert(ExternalInteractionCallbackLog log);

    Optional<Long> findIdByIdempotencyKey(String idempotencyKey);

    void updateResponse(
            long id,
            String mobileNo,
            Long userId,
            String responseCode,
            String responseMsg,
            String responseRef,
            boolean success,
            int durationMs
    );
}
