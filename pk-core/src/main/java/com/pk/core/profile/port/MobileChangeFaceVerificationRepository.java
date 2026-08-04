package com.pk.core.profile.port;

import java.time.Instant;
import java.util.Optional;

public interface MobileChangeFaceVerificationRepository {
    void insert(FaceVerificationInsert insert);

    Optional<FaceVerificationData> findLatestPromotedByUserId(long userId);

    Optional<FaceVerificationData> findVerifiedByToken(String faceVerifyToken);

    void attachOtpToken(String faceVerifyToken, String otpToken);

    boolean promote(String faceVerifyToken, Instant promotedAt);

    record FaceVerificationInsert(
            String faceVerifyToken,
            long userId,
            String partnerUserId,
            String requestId,
            String deviceNo,
            String livenessId,
            String livenessResult,
            String livenessSequenceId,
            String baselineType,
            String baselineFaceEncryptedRef,
            String candidateFaceEncryptedRef,
            Double similarity,
            String status,
            Instant expiresAt
    ) {
    }

    record FaceVerificationData(
            long id,
            String faceVerifyToken,
            long userId,
            String partnerUserId,
            String requestId,
            String deviceNo,
            String livenessId,
            String baselineType,
            String baselineFaceEncryptedRef,
            String candidateFaceEncryptedRef,
            Double similarity,
            String status,
            String otpToken,
            Instant expiresAt,
            Instant promotedAt
    ) {
        public boolean usableAt(Instant now) {
            return "VERIFIED_PENDING".equals(status) && expiresAt != null && expiresAt.isAfter(now);
        }
    }
}
