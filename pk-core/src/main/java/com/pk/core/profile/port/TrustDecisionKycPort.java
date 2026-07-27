package com.pk.core.profile.port;

import com.pk.core.profile.ocr.OcrSessionState;

public interface TrustDecisionKycPort {
    OcrResult checkIdentityCard(byte[] imageBytes);

    LivenessResult checkLiveness(byte[] imageBytes);

    FaceCompareResult compareFaces(byte[] idCardImage, byte[] faceImage);

    record OcrResult(
            String result,
            String sequenceId,
            String rawJson,
            OcrSessionState.OcrParsedFields parsed
    ) {
    }

    record LivenessResult(String result, double score, String sequenceId) {
    }

    record FaceCompareResult(String result, double similarity, String sequenceId) {
    }
}
