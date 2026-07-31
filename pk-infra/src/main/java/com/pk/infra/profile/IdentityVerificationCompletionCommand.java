package com.pk.infra.profile;

import com.pk.core.profile.EncryptedField;
import com.pk.core.profile.ocr.OcrSessionState;
import com.pk.core.profile.sync.LenderDeviceContext;

public record IdentityVerificationCompletionCommand(
        long userId,
        String partnerUserId,
        String mobileNo,
        String requestId,
        String fullName,
        String plainIdNo,
        EncryptedField encryptedIdNo,
        String idNoHash,
        String channel,
        Long ocrVendorCallLogId,
        OcrSessionState.OcrParsedFields parsed,
        String lenderRawOcrDetail,
        byte[] idCardImage,
        byte[] faceImage,
        String livenessId,
        LenderDeviceContext device
) {
}
