package com.pk.core.profile.port;

import com.pk.core.profile.ocr.OcrVendorCallStatus;
import com.pk.core.profile.ocr.OcrVendorOperationType;
import java.math.BigDecimal;

public interface OcrVendorCallLogWriter {
    void write(OcrVendorCallLogEntry entry);

    record OcrVendorCallLogEntry(
            Long profileId,
            String partnerUserId,
            String mobileNo,
            OcrVendorOperationType operationType,
            String channel,
            String traceId,
            String clientRequestId,
            OcrVendorCallStatus status,
            String apiCode,
            String vendorCode,
            String vendorMessage,
            BigDecimal score,
            BigDecimal threshold,
            String endpoint,
            Integer httpStatus,
            Integer durationMs,
            String requestJson,
            String responseJson,
            String idCardImageEncryptedRef,
            String livenessImageEncryptedRef
    ) {
    }
}
