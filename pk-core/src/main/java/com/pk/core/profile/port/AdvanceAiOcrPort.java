package com.pk.core.profile.port;

public interface AdvanceAiOcrPort {
    LicenseTokenResult getLicenseToken(Long licenseEffectiveSeconds);

    String ocrCheckIdCard(byte[] imageBytes);

    LivenessResult livenessCheck(String livenessId);

    FaceCompareResult compareFaces(byte[] idCardImage, byte[] faceImage);

    record LicenseTokenResult(String licenseToken, long effectiveSeconds) {
    }

    record LivenessResult(int livenessScore, String detectionResult) {
    }

    record FaceCompareResult(double similarity) {
    }
}
