package com.pk.core.profile.port;

import com.pk.core.profile.ocr.OcrSessionState;
import java.util.List;

public interface TrustDecisionKycPort {
    OcrResult checkIdentityCard(byte[] imageBytes);

    LivenessLicense obtainLivenessLicense(int sessionDurationSeconds);

    SdkLivenessResult retrieveLivenessResult(String livenessId);

    record OcrResult(
            String result,
            String sequenceId,
            String rawJson,
            OcrSessionState.OcrParsedFields parsed
    ) {
    }

    record LivenessLicense(String license, long expiryTimestamp, String sequenceId) {
    }

    record SdkLivenessResult(
            String result,
            String sequenceId,
            byte[] faceImage,
            List<String> deviceRiskTags,
            int deviceRiskLevel
    ) {
    }
}
